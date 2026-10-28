package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer.BatchResult;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class RequestScheduler {
    /**
     * Constant for one hundred as double.
     */
    private static final double DOUBLE_100 = 100d;
    /**
     * Constant for one hundred as double.
     */
    private static final int INTEGER_1024 = 1024;
    /**
     * Constant for the value of max batch request size (20 MB).
     */
    private static final int MAX_BATCH_REQUEST_BODY_SIZE = 20 * INTEGER_1024 * INTEGER_1024;
    /**
     * How long an idle queue thread is kept alive (in seconds).
     */
    private static final long THREAD_KEEP_ALIVE_SECONDS = 60L;
    /**
     * How long close() waits for the pending flush tasks to finish (in seconds).
     */
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;
    /**
     * The number of resend attempts.
     */
    private final int maxAttempt;
    /**
     * The interval of request resend in milliseconds.
     */
    private final int attemptTimeout;
    /**
     * The maximum request number per batch.
     */
    private final int maxBatchSize;
    /**
     * Number of separate request queues
     */
    private final int numQueues;
    /**
     * HTTP user agent string.
     */
    private final String userAgent;
    /**
     * HTTP header for Sec-CH-UA
     */
    private final String clientHintUserAgent;
    /**
     * Remote address (ip) from the client.
     */
    private final String remoteAddress;
    /**
     * Maximum number of requests per queue, 0 = unlimited.
     */
    private final int maxQueueSize;
    /**
     * Mapp Intelligence request queues.
     */
    private final Map<Integer, ConcurrentLinkedDeque<String>> queues = new ConcurrentHashMap<>();
    /**
     * Size of each request queue.
     *
     * <p>{@code ConcurrentLinkedDeque.size()} has to walk the whole deque - it is explicitly not a
     * constant time operation. Calling it on every {@code add} makes inserting O(n) and filling a
     * queue O(n squared), which matters for the cron job that inserts a log file line by line.</p>
     */
    private final Map<Integer, AtomicInteger> queueSizes = new ConcurrentHashMap<>();
    /**
     * Mapp Intelligence debug logger.
     */
    private final MappIntelligenceDebugLogger logger;
    /**
     * Mapp Intelligence consumer.
     */
    private final MappIntelligenceConsumer consumer;
    /**
     * Whether this scheduler created the consumer and therefore has to close it. A consumer passed in
     * from outside may be shared by several instances and belongs to the caller.
     */
    private final boolean ownsConsumer;
    /**
     * An ExecutorService that executes each submitted task using one of possibly several pooled threads.
     */
    private final Map<Integer, ThreadPoolExecutor> executor = new ConcurrentHashMap<>();
    /**
     * Callback for complete flushing.
     */
    private volatile BiConsumer<Boolean, Integer> onFlushComplete = null;
    /**
     * Scheduler for the periodic flush, null if no requestInterval is configured.
     */
    private final ScheduledExecutorService scheduler;
    /**
     * Makes close() idempotent - it is reachable both explicitly and from the shutdown hook.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);
    /**
     * Requests of the discarded batches of all queues, null as long as they are not collected (see
     * {@link #collectDiscardedRequests()}).
     */
    private volatile List<String> discardedRequests = null;

    /**
     * @param config Mapp Intelligence configuration
     * @param c Mapp Intelligence consumer, closed together with this scheduler
     */
    public RequestScheduler(Map<String, Object> config, MappIntelligenceConsumer c) {
        this(config, c, true);
    }

    /**
     * @param config       Mapp Intelligence configuration
     * @param c            Mapp Intelligence consumer
     * @param ownsConsumer Whether {@link #close()} closes the consumer as well
     */
    public RequestScheduler(Map<String, Object> config, MappIntelligenceConsumer c, boolean ownsConsumer) {
        this.maxAttempt = (int) config.get("maxAttempt");
        this.attemptTimeout = (int) config.get("attemptTimeout");
        this.maxBatchSize = (int) config.get("maxBatchSize");
        this.maxQueueSize = Math.max(0, (int) config.getOrDefault("maxQueueSize", 0));
        this.numQueues = ((int) config.get("requestQueues")) + 1;
        this.consumer = c;
        this.ownsConsumer = ownsConsumer;

        this.userAgent = (String) config.get("userAgent");
        this.remoteAddress = (String) config.get("remoteAddress");
        this.clientHintUserAgent = (String) config.get("clientHintUserAgent");

        int logLevel = (int) config.get("logLevel");
        MappIntelligenceLogger l = (MappIntelligenceLogger) config.get("logger");
        this.logger = new MappIntelligenceDebugLogger(l, logLevel);

        this.init();
        this.scheduler = this.executeAtFixedRate((int) config.get("requestInterval"));
    }

    /**
     * @param name Thread name
     *
     * @return A factory for daemon threads, so a caller who forgets close() cannot keep the JVM alive
     */
    private static ThreadFactory daemonThreadFactory(final String name) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, name);
                thread.setDaemon(true);

                return thread;
            }
        };
    }

    /**
     *
     */
    private void offerTask(int queueId) {
        try {
            this.executor.get(queueId).execute(() -> this.flushTask(queueId));
        } catch (RejectedExecutionException e) {
            // a flush of this queue is already pending and sends the new requests as well
            this.logger.debug(MappIntelligenceMessages.TASK_REJECTED, queueId);
        }
    }

    /**
     *
     */
    private void offerTasks() {
        for (int i = 0; i < this.numQueues; i++) {
            final int queueId = i;
            try {
                this.executor.get(i).execute(() -> this.flushTask(queueId));
            } catch (RejectedExecutionException e) {
                // Each queue executor only accepts two pending tasks, so concurrent flush() calls or
                // a flush() colliding with the requestInterval scheduler are rejected. A task that is
                // already queued picks up the new requests, so nothing is lost and this flush is
                // effectively merged into the pending one. It used to be logged at debug only, which
                // made it invisible in production.
                this.logger.warn(MappIntelligenceMessages.TASK_REJECTED, queueId);
            }
        }
    }

    /**
     * @param wasRequestSuccessful Whether everything of this queue was delivered
     * @param queueId              ID of the request queue
     */
    private void notifyFlushComplete(boolean wasRequestSuccessful, int queueId) {
        BiConsumer<Boolean, Integer> callback = this.onFlushComplete;
        if (callback != null) {
            callback.accept(wasRequestSuccessful, queueId);
        }
    }

    /**
     *
     */
    private void init() {
        for (int i = 0; i < this.numQueues; i++) {
            this.queues.put(i, new ConcurrentLinkedDeque<>());
            this.queueSizes.put(i, new AtomicInteger(0));

            ThreadPoolExecutor queueExecutor = new ThreadPoolExecutor(
                1,
                1,
                THREAD_KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2),
                daemonThreadFactory("mapp-intelligence-queue-" + i),
                new ThreadPoolExecutor.AbortPolicy()
            );
            // without this the single core thread never dies, so an abandoned scheduler stays
            // reachable through its own worker thread and can never be garbage collected
            queueExecutor.allowCoreThreadTimeOut(true);

            this.executor.put(i, queueExecutor);
        }
    }

    /**
     * @param period The period between successive executions
     */
    private ScheduledExecutorService executeAtFixedRate(int period) {
        if (period <= 0) {
            return null;
        }

        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(
            1,
            daemonThreadFactory("mapp-intelligence-interval")
        );
        scheduledExecutor.scheduleAtFixedRate(this::offerTasks, period, period, TimeUnit.SECONDS);

        return scheduledExecutor;
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    private BatchResult sendBatch(List<String> batchContent) {
        if (this.consumer != null) {
            BatchResult result;
            try {
                result = this.consumer.send(batchContent);
            } catch (RuntimeException e) {
                // the batch is already out of the queue - without this a throwing custom consumer
                // loses it, and the exception kills the flush of everything behind it
                this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                result = BatchResult.RETRY;
            }

            return (result != null) ? result : BatchResult.RETRY;
        }
        else {
            this.logger.error(MappIntelligenceMessages.UNDEFINED_CONSUMER_TYPE);
        }

        return BatchResult.RETRY;
    }

    /**
     * @param queueId ID of the request queue
     *
     * @return Current number of requests in that queue
     */
    private int getQueueSize(int queueId) {
        AtomicInteger queueSize = this.queueSizes.get(queueId);

        return (queueSize != null) ? queueSize.get() : 0;
    }

    /**
     * @param queueId ID of the request queue
     * @param delta   Change of the queue size
     *
     * @return The new size, never below zero
     */
    private int updateQueueSize(int queueId, int delta) {
        AtomicInteger queueSize = this.queueSizes.get(queueId);
        if (queueSize == null) {
            return 0;
        }

        return queueSize.updateAndGet(size -> Math.max(0, size + delta));
    }

    /**
     * @param queue request queue
     * @param queueId ID of the request queue
     *
     * @return String LinkedList with tracking requests
     */
    private LinkedList<String> getBatchContent(ConcurrentLinkedDeque<String> queue, int queueId) {
        LinkedList<String> batchContent = new LinkedList<>();

        // long, not float: above 2^24 a float can no longer represent every integer, so the sum drifts
        // away from the real payload size just where the 20 MB limit is checked
        long currentBatchSize = 0;
        // bounded by the deque itself, not by the counter: add() may already have put a request into
        // the deque that the counter does not show yet, and a batch sized by the counter would leave
        // it behind although flushQueue() then reports the queue as done
        for (int i = 0; i < this.maxBatchSize; i++) {
            String request = queue.poll();
            if (request == null) {
                break;
            }

            this.updateQueueSize(queueId, -1);
            batchContent.add(request);

            currentBatchSize += (request + System.lineSeparator()).length();
            if (currentBatchSize >= MAX_BATCH_REQUEST_BODY_SIZE) {
                this.logger.info(String.format(
                    MappIntelligenceMessages.MAX_BATCH_PAYLOAD_LIMIT_REACHED,
                    queueId,
                    Math.round((double) currentBatchSize / INTEGER_1024 / INTEGER_1024 * DOUBLE_100) / DOUBLE_100,
                    batchContent.size()
                ));
                break;
            }
        }

        return batchContent;
    }

    /**
     * @param queue          request queue
     * @param queueId        ID of the request queue
     * @param discardedBatch Set to true as soon as a batch of this queue is discarded
     *
     * @return true if the queue was drained, false if a batch went back into the queue
     */
    private boolean flushQueue(ConcurrentLinkedDeque<String> queue, int queueId, AtomicBoolean discardedBatch) {
        boolean wasRequestSuccessful = true;
        this.logger.debug(MappIntelligenceMessages.SENT_BATCH_REQUESTS, queueId, this.getQueueSize(queueId));

        // isEmpty() is constant time and always exact, so loop termination never depends on the counter
        while (wasRequestSuccessful && !queue.isEmpty()) {
            LinkedList<String> batchContent = this.getBatchContent(queue, queueId);
            if (batchContent.isEmpty()) {
                // nothing could be taken although the queue is not empty - without this the loop
                // would spin without ever sending anything
                break;
            }

            int batchSize = batchContent.size();
            BatchResult result = this.sendBatch(batchContent);

            if (result == BatchResult.DISCARDED) {
                // the batch leaves the queue so it cannot block the rest, but it was never delivered
                discardedBatch.set(true);
                List<String> discarded = this.discardedRequests;
                if (discarded != null) {
                    // one call on the synchronized list: the requests of a batch stay together, in their order
                    discarded.addAll(batchContent);
                }
            } else if (result == BatchResult.RETRY) {
                wasRequestSuccessful = false;
                this.logger.warn(MappIntelligenceMessages.BATCH_REQUEST_FAILED, queueId);
                for (Iterator<String> iter = batchContent.descendingIterator(); iter.hasNext(); ){
                    // count first, then insert - see add()
                    this.updateQueueSize(queueId, 1);
                    queue.addFirst(iter.next());
                }
            }

            this.logger.debug(
                MappIntelligenceMessages.CURRENT_QUEUE_STATUS,
                batchSize,
                queueId,
                this.getQueueSize(queueId)
            );
        }

        if (queue.isEmpty()) {
            this.logger.debug(MappIntelligenceMessages.QUEUE_IS_EMPTY, queueId);
        }

        return wasRequestSuccessful;
    }

    /**
     * Synchronizing access to shared queue resource.
     *
     * @param queueId ID of the request queue
     */
    private void flushTask(int queueId) {
        int currentAttempt = 0;
        boolean wasRequestSuccessful = false;
        boolean interrupted = false;
        // remembered across attempts: a retry only resends what went back into the queue, a batch
        // discarded in an earlier attempt is gone and must still fail the flush
        AtomicBoolean discardedBatch = new AtomicBoolean(false);

        try {
            while (!wasRequestSuccessful && currentAttempt < this.maxAttempt) {
                wasRequestSuccessful = this.flushQueue(this.queues.get(queueId), queueId, discardedBatch);
                currentAttempt++;

                if (!wasRequestSuccessful) {
                    try {
                        this.logger.debug(
                            MappIntelligenceMessages.FLUSH_WAS_NOT_SUCCESSFUL,
                            queueId,
                            currentAttempt,
                            this.maxAttempt
                        );

                        if (currentAttempt < this.maxAttempt) {
                            this.logger.debug(
                                MappIntelligenceMessages.TRY_AGAIN,
                                queueId,
                                this.attemptTimeout
                            );
                            Thread.sleep(this.attemptTimeout);
                        }
                    } catch (InterruptedException e) {
                        interrupted = true;
                        this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                    }
                }
            }
        } finally {
            if (interrupted) {
                this.logger.debug(MappIntelligenceMessages.CURRENT_THREAD_WAS_INTERRUPT);
                Thread.currentThread().interrupt();
            }

            if (!wasRequestSuccessful) {
                this.discardOldestRequests(queueId);
            }

            // in a finally block on purpose: a caller that waits for one callback per queue would
            // wait forever if an unexpected exception skipped the notification
            this.notifyFlushComplete(wasRequestSuccessful && !discardedBatch.get(), queueId);
        }
    }


    /**
     * @param value Value
     *
     * @return Result
     */
    private int calculateModulo(String value) {
        BigInteger bigInteger = new BigInteger(value);
        int mod = numQueues - 1;

        return bigInteger.mod(BigInteger.valueOf(mod)).intValue();
    }

    /**
     * @param request Request
     *
     * @return Queue ID
     */
    private int getQueueIdFromRequest(String request) {
        if (this.numQueues <= 1) {
            return 0;
        }

        String urlString = "https://localhost/" + request;
        String paramValue = URLString.getQueryParam(urlString, "eid");
        if (!paramValue.isEmpty()) {
            long queueId;
            try {
                queueId = this.calculateModulo(paramValue);
            }
            catch (NumberFormatException e) {
                queueId = this.calculateModulo(paramValue.hashCode() + "");
            }

            return (int) queueId;
        }

        String userAgentString = this.userAgent.isEmpty() ? this.clientHintUserAgent : this.userAgent;
        if (!userAgentString.isEmpty()) {
            return this.calculateModulo((userAgentString + this.remoteAddress).hashCode() + "");
        }

        return this.numQueues - 1;
    }

    /**
     * A full queue that could not be sent is reduced to the maximum queue size by discarding the oldest
     * requests, so the queue cannot grow without limit (e.g. while the track server cannot be reached).
     *
     * @param queueId ID of the request queue
     */
    private void discardOldestRequests(int queueId) {
        if (this.maxQueueSize <= 0) {
            return;
        }

        ConcurrentLinkedDeque<String> queue = this.queues.get(queueId);
        int queueSize = this.queueSizes.get(queueId).get();
        int overflow = queueSize - this.maxQueueSize;
        if (overflow <= 0) {
            return;
        }

        this.logger.warn(MappIntelligenceMessages.MAX_QUEUE_SIZE_REACHED, queueId, queueSize, overflow);
        for (int i = 0; i < overflow && queue.pollFirst() != null; i++) {
            this.updateQueueSize(queueId, -1);
        }
    }

    /**
     * Hands the queues to the executors and returns immediately.
     *
     * <p>Best effort: if a flush for a queue is already pending, this call is merged into it (logged
     * as {@code TASK_REJECTED}) and the pending task sends the new requests as well. Use
     * {@link #close()} when the requests have to be out before continuing - that one waits.</p>
     */
    public void flush() {
        if (this.closed.get()) {
            // the executors are gone, nothing would ever run - answer right away instead of leaving a
            // caller that waits for one callback per queue hanging
            this.logger.warn(MappIntelligenceMessages.FLUSH_AFTER_CLOSE);
            for (int i = 0; i < this.numQueues; i++) {
                this.notifyFlushComplete(this.queues.get(i).isEmpty(), i);
            }

            return;
        }

        this.offerTasks();
    }

    /**
     * Inserts the specified element at the tail of this deque. As the deque is unbounded, this method will never
     * throw IllegalStateException or return false.
     *
     * @param request element whose presence in this collection is to be ensured
     *
     * @return Queue ID, -1 if the request was rejected because this scheduler is closed
     */
    public int add(String request) {
        if (this.closed.get()) {
            // nothing would ever send it - say so instead of letting it rot in the queue
            this.logger.error(MappIntelligenceMessages.REQUEST_AFTER_CLOSE, request);

            return -1;
        }

        int queueId = this.getQueueIdFromRequest(request);

        // count first, then insert: the counter may briefly run ahead of the deque but never behind
        // it, so the poller's decrement can never hit zero early and let the counter drift
        int currentQueueSize = this.updateQueueSize(queueId, 1);
        this.queues.get(queueId).add(request);

        // a full queue is sent first; if that fails, the oldest requests are discarded after the flush
        if (this.maxQueueSize > 0 && currentQueueSize == this.maxQueueSize) {
            this.offerTask(queueId);
        }

        // format lazily: the varargs overload only builds the message once DEBUG is actually enabled
        this.logger.debug(
            MappIntelligenceMessages.ADD_THE_FOLLOWING_REQUEST_TO_QUEUE,
            queueId,
            currentQueueSize,
            request
        );

        return queueId;
    }

    /**
     * @param queueId Queue ID
     *
     * @return Mapp Intelligence request queue
     */
    public Deque<String> getQueue(int queueId) {
        return this.queues.get(queueId);
    }

    /**
     * From now on, the requests of every batch that a flush discards are collected, from all queues (used by the cron
     * job, which keeps them in a file). Without this call nothing is collected, so a long-running application does not
     * accumulate them.
     *
     * @return The collected requests - a thread-safe list that the following flushes fill; the requests of a batch are
     *         in their order. Calling it again returns the same list.
     */
    public synchronized List<String> collectDiscardedRequests() {
        if (this.discardedRequests == null) {
            this.discardedRequests = Collections.synchronizedList(new ArrayList<>());
        }

        return this.discardedRequests;
    }

    /**
     * @param callback Callback for complete flushing
     */
    public void setOnFlushComplete(BiConsumer<Boolean, Integer> callback) {
        this.onFlushComplete = callback;
    }

    /**
     * Flushes the remaining requests and releases the thread pools and - if this scheduler created
     * it - the consumer.
     *
     * <p>In contrast to {@link #flush()} this waits for the pending flush tasks to finish, up to
     * {@value #SHUTDOWN_TIMEOUT_SECONDS} seconds. {@code flush()} only hands the work to the
     * executors and returns immediately, which is why it cannot be used on its own to keep the
     * "sends all requests in the queue" promise during shutdown.</p>
     *
     * <p>Idempotent: the second call does nothing.</p>
     */
    public void close() {
        this.close(true);
    }

    /**
     * Like {@link #close()}, but lets the caller decide whether the requests still in the queues are
     * sent one last time.
     *
     * <p>{@code false} is meant for a caller that keeps its own copy of the requests - the cron job
     * keeps the log file after an unsuccessful run and sends it again next time. A last send there
     * would only deliver requests that the next run delivers again. Flush tasks that are already
     * pending still run to the end either way.</p>
     *
     * @param flushRemaining Whether the requests still in the queues are sent before closing
     */
    public void close(boolean flushRemaining) {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }

        if (this.scheduler != null) {
            this.scheduler.shutdownNow();
        }

        if (flushRemaining) {
            // hand the remaining requests over before refusing new tasks
            this.offerTasks();
        }

        for (ThreadPoolExecutor queueExecutor : this.executor.values()) {
            // no new tasks, but everything already queued still runs
            queueExecutor.shutdown();
        }

        boolean interrupted = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SHUTDOWN_TIMEOUT_SECONDS);
        for (Map.Entry<Integer, ThreadPoolExecutor> entry : this.executor.entrySet()) {
            long remaining = deadline - System.nanoTime();
            try {
                if (remaining <= 0 || !entry.getValue().awaitTermination(remaining, TimeUnit.NANOSECONDS)) {
                    this.logger.warn(String.format(MappIntelligenceMessages.FLUSH_TIMEOUT_ON_CLOSE, entry.getKey()));
                }
            } catch (InterruptedException e) {
                interrupted = true;
                break;
            }
        }

        // the consumer must not be closed before the flush is done, otherwise the writer is gone
        // while the last batch is still being written
        this.closeConsumer();

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes the consumer if this scheduler created it and it holds resources of its own.
     */
    private void closeConsumer() {
        if (!this.ownsConsumer || !(this.consumer instanceof AutoCloseable)) {
            return;
        }

        try {
            ((AutoCloseable) this.consumer).close();
        } catch (Exception e) {
            this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }
    }
}
