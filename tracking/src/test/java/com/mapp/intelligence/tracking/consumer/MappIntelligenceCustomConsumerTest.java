package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer.BatchResult;
import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Covers the contract a custom consumer (consumer type CUSTOM) relies on: what the scheduler does with
 * each answer of {@code sendBatch} / {@code send}, and how the consumer takes part in the lifecycle.
 */
public class MappIntelligenceCustomConsumerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);
    }

    /**
     * Records every batch it receives.
     */
    private static class RecordingConsumer implements MappIntelligenceConsumer {
        final List<List<String>> batches = Collections.synchronizedList(new ArrayList<>());
        volatile boolean deliver = true;

        @Override
        public boolean sendBatch(List<String> batchContent) {
            this.batches.add(new ArrayList<>(batchContent));

            return this.deliver;
        }

        /**
         * @return All requests of all batches, in the order they were sent
         */
        List<String> requests() {
            List<String> requests = new ArrayList<>();
            synchronized (this.batches) {
                for (List<String> batch : this.batches) {
                    requests.addAll(batch);
                }
            }

            return requests;
        }
    }

    /**
     * @param consumer   Custom consumer
     * @param maxAttempt Number of attempts per flush
     *
     * @return Configuration with a single queue, so the order of the requests is deterministic
     */
    private static MappIntelligenceConfig config(MappIntelligenceConsumer consumer, int maxAttempt) {
        return (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(consumer)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxAttempt(maxAttempt)
            .setAttemptTimeout(1)
            .setRequestQueues(0);
    }

    /**
     * @param queue Request queue with a single queue
     *
     * @return Flush result of that queue
     */
    private static boolean flushAndWait(MappIntelligenceQueue queue) throws InterruptedException {
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        queue.setOnFlushComplete((success, queueId) -> {
            result.set(success);
            latch.countDown();
        });

        queue.flush();
        assertTrue("the flush must report back", latch.await(5, TimeUnit.SECONDS));

        return result.get();
    }

    @Test
    public void testDefaultSendDelegatesToSendBatch() {
        MappIntelligenceConsumer delivering = batchContent -> true;
        MappIntelligenceConsumer failing = batchContent -> false;

        assertEquals(BatchResult.SENT, delivering.send(Collections.singletonList("wt?p=400,0")));
        assertEquals(BatchResult.RETRY, failing.send(Collections.singletonList("wt?p=400,0")));
    }

    @Test
    public void testSendBatchOnlyConsumerDeliversEverythingInOrder() throws InterruptedException {
        RecordingConsumer consumer = new RecordingConsumer();
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 1).setMaxBatchSize(3).build());

        for (int i = 0; i < 10; i++) {
            queue.add("wt?p=400," + i);
        }

        assertTrue(flushAndWait(queue));

        List<String> requests = consumer.requests();
        assertEquals(10, requests.size());
        for (int i = 0; i < 10; i++) {
            assertTrue(requests.get(i), requests.get(i).startsWith("wt?p=400," + i));
        }

        // maxBatchSize is respected: 3 + 3 + 3 + 1
        assertEquals(4, consumer.batches.size());
        for (List<String> batch : consumer.batches) {
            assertTrue(batch.size() <= 3);
        }

        queue.close();
    }

    @Test
    public void testFailingSendBatchKeepsTheRequestsForTheNextFlush() throws InterruptedException {
        RecordingConsumer consumer = new RecordingConsumer();
        consumer.deliver = false;
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 3).build());

        queue.add("wt?p=400,0");
        queue.add("wt?p=400,1");

        assertFalse(flushAndWait(queue));
        // one attempt per maxAttempt, and nothing is lost
        assertEquals(3, consumer.batches.size());
        assertEquals(2, queue.getQueue(0).size());

        consumer.deliver = true;
        consumer.batches.clear();

        assertTrue(flushAndWait(queue));
        assertEquals(2, consumer.requests().size());
        assertTrue(consumer.requests().get(0).startsWith("wt?p=400,0"));
        assertTrue(queue.getQueue(0).isEmpty());

        queue.close();
    }

    @Test
    public void testConsumerCanDiscardABatch() throws InterruptedException {
        AtomicInteger calls = new AtomicInteger(0);
        MappIntelligenceConsumer consumer = new MappIntelligenceConsumer() {
            @Override
            public boolean sendBatch(List<String> batchContent) {
                return this.send(batchContent) != BatchResult.RETRY;
            }

            @Override
            public BatchResult send(List<String> batchContent) {
                calls.incrementAndGet();

                return BatchResult.DISCARDED;
            }
        };
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 3).build());

        queue.add("wt?p=400,0");

        // the batch leaves the queue, but the flush must not look successful
        assertFalse(flushAndWait(queue));
        assertTrue(queue.getQueue(0).isEmpty());
        assertEquals("a discarded batch is not retried", 1, calls.get());

        queue.close();
    }

    @Test
    public void testSendReturningNullIsTreatedAsRetry() throws InterruptedException {
        // what a Mockito mock that only stubs sendBatch() does: send() is not run, it returns null
        MappIntelligenceConsumer consumer = new MappIntelligenceConsumer() {
            @Override
            public boolean sendBatch(List<String> batchContent) {
                return true;
            }

            @Override
            public BatchResult send(List<String> batchContent) {
                return null;
            }
        };
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 1).build());

        queue.add("wt?p=400,0");

        assertFalse(flushAndWait(queue));
        assertEquals("the request must not be lost", 1, queue.getQueue(0).size());

        queue.close();
    }

    @Test
    public void testThrowingConsumerDoesNotLoseTheBatch() throws InterruptedException {
        AtomicBoolean fail = new AtomicBoolean(true);
        RecordingConsumer delegate = new RecordingConsumer();
        MappIntelligenceConsumer consumer = batchContent -> {
            if (fail.get()) {
                throw new IllegalStateException("backend not reachable");
            }

            return delegate.sendBatch(batchContent);
        };
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 1).build());

        queue.add("wt?p=400,0");
        queue.add("wt?p=400,1");

        assertFalse(flushAndWait(queue));
        assertEquals("the batch must go back into the queue", 2, queue.getQueue(0).size());

        fail.set(false);
        assertTrue(flushAndWait(queue));
        assertEquals(2, delegate.requests().size());

        queue.close();
    }

    @Test
    public void testCloseFlushesIntoTheCustomConsumerAndLeavesItOpen() {
        class CloseableConsumer extends RecordingConsumer implements AutoCloseable {
            final AtomicBoolean closed = new AtomicBoolean(false);

            @Override
            public void close() {
                this.closed.set(true);
            }
        }

        CloseableConsumer consumer = new CloseableConsumer();
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 1).build());

        for (int i = 0; i < 5; i++) {
            queue.add("wt?p=400," + i);
        }

        // no flush(): close() alone has to send the rest before it returns
        queue.close();

        assertEquals(5, consumer.requests().size());
        assertFalse("a consumer from the config belongs to the caller", consumer.closed.get());
    }

    /**
     * A subclass of a built-in consumer written against the boolean API.
     */
    private static class LegacyHttpConsumer extends MappIntelligenceConsumerHttpClient {
        final AtomicInteger sendBatchCalls = new AtomicInteger(0);
        final boolean callSuper;

        LegacyHttpConsumer(java.util.Map<String, Object> config, boolean callSuper) {
            super(config);
            this.callSuper = callSuper;
        }

        @Override
        public boolean sendBatch(List<String> batchContent) {
            this.sendBatchCalls.incrementAndGet();

            return !this.callSuper || super.sendBatch(batchContent);
        }
    }

    /**
     * @return Configuration whose track domain refuses every connection
     */
    private static java.util.Map<String, Object> unreachableConfig() {
        return (new MappIntelligenceConfig("123451234512345", "127.0.0.1:9"))
            .setForceSSL(false)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .build();
    }

    @Test
    public void testSendBatchOverrideOfABuiltInConsumerStaysInEffect() throws InterruptedException {
        // The scheduler calls send() now. The built-in consumers implement it themselves, so a
        // subclass that only overrides sendBatch() used to be bypassed silently.
        LegacyHttpConsumer consumer = new LegacyHttpConsumer(unreachableConfig(), false);
        MappIntelligenceQueue queue = new MappIntelligenceQueue(config(consumer, 1).build());

        queue.add("wt?p=400,0");
        assertTrue(flushAndWait(queue));

        assertEquals(1, consumer.sendBatchCalls.get());
        queue.close();
    }

    @Test
    public void testSendBatchOverrideCallingSuperDoesNotRecurse() {
        LegacyHttpConsumer consumer = new LegacyHttpConsumer(unreachableConfig(), true);

        // super.sendBatch() must do the actual work instead of going through send() back into the
        // override; the connection error is retryable
        assertEquals(BatchResult.RETRY, consumer.send(Collections.singletonList("wt?p=400,0")));
        assertEquals(1, consumer.sendBatchCalls.get());
    }
}
