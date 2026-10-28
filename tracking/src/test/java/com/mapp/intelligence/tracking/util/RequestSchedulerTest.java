package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceTracking;
import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;
import com.mapp.intelligence.tracking.data.MappIntelligencePage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Covers the flush result of the scheduler when batches are discarded, and its lifecycle edges.
 */
public class RequestSchedulerTest {
    /**
     * Answers every batch from a script, one result per call.
     */
    private static class ScriptedConsumer implements MappIntelligenceConsumer, AutoCloseable {
        private final List<MappIntelligenceConsumer.BatchResult> script;
        private final List<List<String>> batches = Collections.synchronizedList(new ArrayList<>());
        private final AtomicBoolean closed = new AtomicBoolean(false);

        ScriptedConsumer(MappIntelligenceConsumer.BatchResult... script) {
            this.script = Collections.synchronizedList(new ArrayList<>(Arrays.asList(script)));
        }

        @Override
        public boolean sendBatch(List<String> batchContent) {
            return this.send(batchContent) != BatchResult.RETRY;
        }

        @Override
        public BatchResult send(List<String> batchContent) {
            this.batches.add(new ArrayList<>(batchContent));

            return this.script.isEmpty() ? BatchResult.SENT : this.script.remove(0);
        }

        @Override
        public void close() {
            this.closed.set(true);
        }
    }

    /**
     * @param maxAttempt    Number of attempts
     * @param requestQueues Number of request queues (plus the fallback queue)
     *
     * @return Scheduler configuration
     */
    private static Map<String, Object> config(int maxAttempt, int requestQueues) {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setMaxAttempt(maxAttempt)
            .setAttemptTimeout(1)
            .setMaxBatchSize(1);
        mic.setRequestQueues(requestQueues);

        return mic.build();
    }

    /**
     * @param scheduler Scheduler
     * @param numQueues Number of queues that report back
     *
     * @return Flush result per queue
     */
    private static Map<Integer, Boolean> flushAndWait(RequestScheduler scheduler, int numQueues) throws Exception {
        Map<Integer, Boolean> results = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(numQueues);
        scheduler.setOnFlushComplete((success, queueId) -> {
            results.put(queueId, success);
            latch.countDown();
        });

        scheduler.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        return results;
    }

    @Test
    public void testDiscardedBatchFailsTheFlushEvenAfterASuccessfulRetry() throws Exception {
        // attempt 1: the first batch is discarded, the second one has to be retried;
        // attempt 2: the retry succeeds - the discarded batch is still lost
        ScriptedConsumer consumer = new ScriptedConsumer(
            MappIntelligenceConsumer.BatchResult.DISCARDED,
            MappIntelligenceConsumer.BatchResult.RETRY,
            MappIntelligenceConsumer.BatchResult.SENT
        );
        RequestScheduler scheduler = new RequestScheduler(config(3, 0), consumer);
        scheduler.add("wt?p=400,0");
        scheduler.add("wt?p=400,1");

        Map<Integer, Boolean> results = flushAndWait(scheduler, 1);

        assertFalse(results.get(0));
        assertEquals(3, consumer.batches.size());
        assertTrue(scheduler.getQueue(0).isEmpty());
        scheduler.close();
    }

    @Test
    public void testDiscardedBatchIsNotRetried() throws Exception {
        ScriptedConsumer consumer = new ScriptedConsumer(MappIntelligenceConsumer.BatchResult.DISCARDED);
        RequestScheduler scheduler = new RequestScheduler(config(3, 0), consumer);
        scheduler.add("wt?p=400,0");

        Map<Integer, Boolean> results = flushAndWait(scheduler, 1);

        assertFalse(results.get(0));
        assertEquals("a discarded batch must not trigger another attempt", 1, consumer.batches.size());
        scheduler.close();
    }

    @Test
    public void testDiscardInOneQueueDoesNotFailAnotherQueue() throws Exception {
        MappIntelligenceConsumer consumer = new MappIntelligenceConsumer() {
            @Override
            public boolean sendBatch(List<String> batchContent) {
                return true;
            }

            @Override
            public BatchResult send(List<String> batchContent) {
                return batchContent.get(0).contains("bad") ? BatchResult.DISCARDED : BatchResult.SENT;
            }
        };
        // one regular queue plus the fallback queue
        RequestScheduler scheduler = new RequestScheduler(config(1, 1), consumer);
        int badQueue = scheduler.add("wt?p=bad&eid=1");
        int goodQueue = scheduler.add("wt?p=good");
        assertTrue(badQueue != goodQueue);

        Map<Integer, Boolean> results = flushAndWait(scheduler, 2);

        assertFalse(results.get(badQueue));
        assertTrue(results.get(goodQueue));
        scheduler.close();
    }

    @Test
    public void testDiscardedRequestsAreCollectedFromAllQueues() throws Exception {
        MappIntelligenceConsumer consumer = new MappIntelligenceConsumer() {
            @Override
            public boolean sendBatch(List<String> batchContent) {
                return true;
            }

            @Override
            public BatchResult send(List<String> batchContent) {
                return batchContent.get(0).contains("bad") ? BatchResult.DISCARDED : BatchResult.SENT;
            }
        };
        Map<String, Object> config = config(1, 1);
        config.put("maxBatchSize", 2);
        RequestScheduler scheduler = new RequestScheduler(config, consumer);

        // nothing is collected without collectDiscardedRequests()
        scheduler.add("wt?p=bad,0&eid=1");
        flushAndWait(scheduler, 2);

        List<String> discarded = scheduler.collectDiscardedRequests();
        assertTrue(discarded.isEmpty());
        assertTrue("the same list", discarded == scheduler.collectDiscardedRequests());

        // one batch of each queue is discarded, the batch of the good requests is not collected
        int queue1 = scheduler.add("wt?p=bad,1&eid=1");
        scheduler.add("wt?p=bad,2&eid=1");
        int queue2 = scheduler.add("wt?p=bad,3");
        scheduler.add("wt?p=bad,4");
        scheduler.add("wt?p=good,5");
        assertTrue(queue1 != queue2);
        flushAndWait(scheduler, 2);

        assertEquals(4, discarded.size());
        // the requests of a batch stay in their order
        assertTrue(discarded.toString(), discarded.indexOf("wt?p=bad,1&eid=1") + 1 == discarded.indexOf("wt?p=bad,2&eid=1"));
        assertTrue(discarded.toString(), discarded.indexOf("wt?p=bad,3") + 1 == discarded.indexOf("wt?p=bad,4"));
        assertFalse(discarded.contains("wt?p=good,5"));
        scheduler.close();
    }

    @Test
    public void testConsumerFromConfigIsNotClosed() {
        ScriptedConsumer consumer = new ScriptedConsumer();
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setConsumer(consumer);

        MappIntelligenceTracking first = new MappIntelligenceTracking(mic);
        first.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.1")));
        first.close();

        assertFalse("a shared consumer belongs to the caller", consumer.closed.get());

        MappIntelligenceTracking second = new MappIntelligenceTracking(mic);
        second.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.2")));
        second.close();

        assertEquals(2, consumer.batches.size());
    }

    @Test
    public void testOwnConsumerIsClosed() {
        ScriptedConsumer consumer = new ScriptedConsumer();
        RequestScheduler scheduler = new RequestScheduler(config(1, 0), consumer, true);
        scheduler.close();

        assertTrue(consumer.closed.get());
    }

    @Test
    public void testAddAfterCloseIsRejected() {
        ScriptedConsumer consumer = new ScriptedConsumer();
        RequestScheduler scheduler = new RequestScheduler(config(1, 0), consumer);
        scheduler.close();

        assertEquals(-1, scheduler.add("wt?p=400,0"));
        assertTrue(scheduler.getQueue(0).isEmpty());
    }

    @Test
    public void testFlushAfterCloseStillReportsBack() throws Exception {
        ScriptedConsumer consumer = new ScriptedConsumer();
        RequestScheduler scheduler = new RequestScheduler(config(1, 0), consumer);
        scheduler.close();

        Map<Integer, Boolean> results = flushAndWait(scheduler, 1);

        assertTrue(results.get(0));
    }

    @Test
    public void testCloseWithoutFlushDoesNotSendTheRemainingRequests() {
        ScriptedConsumer consumer = new ScriptedConsumer();
        RequestScheduler scheduler = new RequestScheduler(config(1, 0), consumer);
        scheduler.add("wt?p=400,0");
        scheduler.add("wt?p=400,1");

        scheduler.close(false);

        assertTrue(consumer.batches.isEmpty());
        assertTrue("the thread pools and the own consumer are released anyway", consumer.closed.get());
        assertEquals(-1, scheduler.add("wt?p=400,2"));
    }

    @Test
    public void testConcurrentAddAndFlushDeliversEveryRequestExactlyOnce() throws Exception {
        final int producers = 4;
        final int requestsPerProducer = 2500;
        ScriptedConsumer consumer = new ScriptedConsumer();
        final RequestScheduler scheduler = new RequestScheduler(config(1, 0), consumer);

        final AtomicBoolean producing = new AtomicBoolean(true);
        Thread flusher = new Thread(() -> {
            while (producing.get()) {
                scheduler.flush();
                Thread.yield();
            }
        });
        flusher.start();

        List<Thread> threads = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            final int producer = p;
            Thread thread = new Thread(() -> {
                for (int i = 0; i < requestsPerProducer; i++) {
                    scheduler.add("wt?p=400," + producer + "-" + i);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        producing.set(false);
        flusher.join();

        // close() sends whatever the concurrent flushes left and waits until every task is done
        scheduler.close();

        int delivered = 0;
        synchronized (consumer.batches) {
            for (List<String> batch : consumer.batches) {
                delivered += batch.size();
            }
        }
        assertEquals(producers * requestsPerProducer, delivered);
        assertTrue(scheduler.getQueue(0).isEmpty());

        java.lang.reflect.Field sizes = RequestScheduler.class.getDeclaredField("queueSizes");
        sizes.setAccessible(true);
        assertEquals(
            "the size counter must not drift under concurrency",
            "0",
            String.valueOf(((Map<?, ?>) sizes.get(scheduler)).get(0))
        );
    }
}
