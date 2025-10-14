package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
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
     * Mapp Intelligence request queues.
     */
    private final Map<Integer, ConcurrentLinkedDeque<String>> queues = new ConcurrentHashMap<>();
    /**
     * Mapp Intelligence debug logger.
     */
    private final MappIntelligenceDebugLogger logger;
    /**
     * Mapp Intelligence consumer.
     */
    private final MappIntelligenceConsumer consumer;
    /**
     * An ExecutorService that executes each submitted task using one of possibly several pooled threads.
     */
    private final Map<Integer, ThreadPoolExecutor> executor = new ConcurrentHashMap<>();
    /**
     * Callback for complete flushing.
     */
    private BiConsumer<Boolean, Integer> onFlushComplete = null;

    /**
     * @param config Mapp Intelligence configuration
     * @param c Mapp Intelligence consumer
     */
    public RequestScheduler(Map<String, Object> config, MappIntelligenceConsumer c) {
        this.maxAttempt = (int) config.get("maxAttempt");
        this.attemptTimeout = (int) config.get("attemptTimeout");
        this.maxBatchSize = (int) config.get("maxBatchSize");
        this.numQueues = ((int) config.get("requestQueues")) + 1;
        this.consumer = c;

        this.userAgent = (String) config.get("userAgent");
        this.remoteAddress = (String) config.get("remoteAddress");
        this.clientHintUserAgent = (String) config.get("clientHintUserAgent");

        int logLevel = (int) config.get("logLevel");
        MappIntelligenceLogger l = (MappIntelligenceLogger) config.get("logger");
        this.logger = new MappIntelligenceDebugLogger(l, logLevel);

        this.init();
        this.executeAtFixedRate((int) config.get("requestInterval"));
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
                this.logger.debug(String.format(MappIntelligenceMessages.TASK_REJECTED, queueId));
            }
        }
    }

    /**
     *
     */
    private void init() {
        for (int i = 0; i < this.numQueues; i++) {
            this.queues.put(i, new ConcurrentLinkedDeque<>());
            this.executor.put(i, new ThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(2),
                new ThreadPoolExecutor.AbortPolicy()
            ));
        }
    }

    /**
     * @param period The period between successive executions
     */
    private void executeAtFixedRate(int period) {
        if (period > 0) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::offerTasks, period, period, TimeUnit.SECONDS);
        }
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    private boolean sendBatch(List<String> batchContent) {
        if (this.consumer != null) {
            return this.consumer.sendBatch(batchContent);
        }
        else {
            this.logger.error(MappIntelligenceMessages.UNDEFINED_CONSUMER_TYPE);
        }

        return false;
    }

    /**
     * @param queue request queue
     * @param queueId ID of the request queue
     *
     * @return String LinkedList with tracking requests
     */
    private LinkedList<String> getBatchContent(ConcurrentLinkedDeque<String> queue, int queueId) {
        LinkedList<String> batchContent = new LinkedList<>();

        float currentBatchSize = 0;
        int batchSize = Integer.min(this.maxBatchSize, queue.size());
        for (int i = 0; i < batchSize; i++) {
            String request = queue.poll();
            if (request != null) {
                batchContent.add(request);

                currentBatchSize += (request + System.lineSeparator()).length();
                if (currentBatchSize >= MAX_BATCH_REQUEST_BODY_SIZE) {
                    this.logger.info(String.format(
                        MappIntelligenceMessages.MAX_BATCH_PAYLOAD_LIMIT_REACHED,
                        queueId,
                        Math.round(currentBatchSize / INTEGER_1024 / INTEGER_1024 * DOUBLE_100) / DOUBLE_100,
                        batchContent.size()
                    ));
                    break;
                }
            }
        }

        return batchContent;
    }

    /**
     * @param queue request queue
     * @param queueId ID of the request queue
     *
     * @return boolean
     */
    private boolean flushQueue(ConcurrentLinkedDeque<String> queue, int queueId) {
        int currentQueueSize = queue.size();
        boolean wasRequestSuccessful = true;
        this.logger.debug(String.format(MappIntelligenceMessages.SENT_BATCH_REQUESTS, queueId, currentQueueSize));

        while (currentQueueSize > 0 && wasRequestSuccessful) {
            LinkedList<String> batchContent = this.getBatchContent(queue, queueId);
            int batchSize = batchContent.size();
            currentQueueSize = queue.size();

            if (!batchContent.isEmpty()) {
                wasRequestSuccessful = this.sendBatch(batchContent);
            }

            if (!wasRequestSuccessful) {
                this.logger.warn(String.format(MappIntelligenceMessages.BATCH_REQUEST_FAILED, queueId));
                for (Iterator<String> iter = batchContent.descendingIterator(); iter.hasNext(); ){
                    queue.addFirst(iter.next());
                }
            }

            this.logger.debug(String.format(
                MappIntelligenceMessages.CURRENT_QUEUE_STATUS,
                batchSize,
                queueId,
                queue.size()
            ));
        }

        if (queue.isEmpty()) {
            this.logger.debug(String.format(MappIntelligenceMessages.QUEUE_IS_EMPTY, queueId));
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

        try {
            while (!wasRequestSuccessful && currentAttempt < this.maxAttempt) {
                wasRequestSuccessful = this.flushQueue(this.queues.get(queueId), queueId);
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
        }

        if (this.onFlushComplete != null) {
            this.onFlushComplete.accept(wasRequestSuccessful, queueId);
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
     * Synchronizing access to shared queue resource.
     */
    public void flush() {
        this.offerTasks();
    }

    /**
     * Inserts the specified element at the tail of this deque. As the deque is unbounded, this method will never
     * throw IllegalStateException or return false.
     *
     * @param request element whose presence in this collection is to be ensured
     *
     * @return Queue ID
     */
    public int add(String request) {
        int queueId = this.getQueueIdFromRequest(request);

        this.queues.get(queueId).add(request);
        int currentQueueSize = this.queues.get(queueId).size();

        this.logger.debug(String.format(MappIntelligenceMessages.ADD_THE_FOLLOWING_REQUEST_TO_QUEUE, queueId, currentQueueSize, request));

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
     * @param callback Callback for complete flushing
     */
    public void setOnFlushComplete(BiConsumer<Boolean, Integer> callback) {
        this.onFlushComplete = callback;
    }
}
