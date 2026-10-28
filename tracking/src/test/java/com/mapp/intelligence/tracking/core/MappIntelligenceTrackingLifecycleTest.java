package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;
import com.mapp.intelligence.tracking.data.MappIntelligencePage;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MappIntelligenceTrackingLifecycleTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final String tempFilePath = System.getProperty("user.dir") + "/src/test/resources/";
    private final String tempFilePrefix = "mapp_intelligence_lifecycle_test";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);

        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");

        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".lock");
        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".log");
    }

    /**
     * @return A tracking instance that writes into its own temporary log file
     */
    private MappIntelligenceTracking newFileTracking() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setFilePath(this.tempFilePath)
            .setFilePrefix(this.tempFilePrefix)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);

        return new MappIntelligenceTracking(mic);
    }

    /**
     * @return All non-empty lines that were written to the log files
     */
    private List<String> writtenLines() throws IOException {
        List<String> lines = new ArrayList<>();
        File dir = new File(this.tempFilePath);
        FilenameFilter filter = (f, name) -> name.startsWith(this.tempFilePrefix)
            && (name.endsWith(".tmp") || name.endsWith(".log"));
        File[] files = dir.listFiles(filter);

        if (files == null) {
            return lines;
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
            }
        }

        return lines;
    }

    /**
     * @param tracking Tracking instance
     * @return The queue executors of the instance
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, ThreadPoolExecutor> getExecutors(MappIntelligenceTracking tracking) throws Exception {
        Field queueField = MappIntelligenceTracking.class.getSuperclass().getDeclaredField("queue");
        queueField.setAccessible(true);
        Object queue = queueField.get(tracking);

        Field schedulerField = queue.getClass().getDeclaredField("requestScheduler");
        schedulerField.setAccessible(true);
        Object scheduler = schedulerField.get(queue);

        Field executorField = scheduler.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);

        return (Map<Integer, ThreadPoolExecutor>) executorField.get(scheduler);
    }

    /**
     * Condition that is polled while the garbage collector is being pushed.
     */
    private interface Condition {
        boolean isMet() throws Exception;
    }

    /**
     * Requests garbage collection until the condition is met, for at most ten seconds.
     *
     * @param condition Condition to wait for
     * @return true if the condition was met in time
     */
    private static boolean collectGarbageUntil(Condition condition) throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            System.gc();
            if (condition.isMet()) {
                return true;
            }
            Thread.sleep(50);
        }

        return condition.isMet();
    }

    /**
     * @param prefix Thread name prefix
     * @return Number of live threads whose name starts with the prefix
     */
    private static int countThreads(String prefix) {
        int count = 0;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.isAlive() && thread.getName().startsWith(prefix)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Tracks without flush() or close() and drops the instance - in a method of its own, so no local
     * variable of the test keeps it reachable.
     *
     * @param requests Number of requests
     */
    private void trackAndAbandon(int requests) {
        MappIntelligenceTracking tracking = this.newFileTracking();
        for (int i = 0; i < requests; i++) {
            assertTrue(tracking.track((new MappIntelligenceDataMap())
                .page(new MappIntelligencePage("en.page." + i))
            ) > -1);
        }
    }

    /**
     * Creates an instance with a periodic flush and drops it without close().
     */
    private void createIntervalInstanceAndAbandon() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setFilePath(this.tempFilePath)
            .setFilePrefix(this.tempFilePrefix)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            // long enough that the interval never fires during the test
            .setRequestInterval(3600);

        new MappIntelligenceTracking(mic);
    }

    @Test
    public void testAbandonedInstanceSendsItsRequestsAfterGarbageCollection() throws Exception {
        // The registry only holds the instance weakly, so an instance that is never closed can be
        // collected. Its queued requests used to be collected with it - now the queue outlives the
        // instance and is closed, which sends them.
        this.trackAndAbandon(3);

        assertTrue(
            "the queued requests of an abandoned instance were lost",
            collectGarbageUntil(() -> this.writtenLines().size() == 3)
        );
    }

    @Test
    public void testAbandonedInstanceWithRequestIntervalReleasesItsThread() throws Exception {
        // The interval thread never times out and keeps its scheduler reachable, so without close()
        // it used to live until the JVM terminated - one thread per abandoned instance.
        final int before = countThreads("mapp-intelligence-interval");

        this.createIntervalInstanceAndAbandon();
        assertTrue(countThreads("mapp-intelligence-interval") > before);

        assertTrue(
            "the interval thread of an abandoned instance is still alive",
            collectGarbageUntil(() -> countThreads("mapp-intelligence-interval") <= before)
        );
    }

    @Test
    public void testCloseWaitsUntilAllRequestsAreWritten() throws Exception {
        final int requests = 25;
        MappIntelligenceTracking tracking = this.newFileTracking();

        for (int i = 0; i < requests; i++) {
            assertTrue(tracking.track((new MappIntelligenceDataMap())
                .page(new MappIntelligencePage("en.page." + i))
            ) > -1);
        }

        // flush() only hands the work to the executors and returns, close() has to wait for it
        tracking.close();

        // no sleep on purpose: if close() did not wait, the file is still incomplete here
        assertEquals(requests, this.writtenLines().size());
    }

    @Test
    public void testCloseIsIdempotent() throws Exception {
        MappIntelligenceTracking tracking = this.newFileTracking();
        assertTrue(tracking.track((new MappIntelligenceDataMap())
            .page(new MappIntelligencePage("en.page.test"))
        ) > -1);

        tracking.close();
        tracking.close();
        tracking.close();

        assertEquals(1, this.writtenLines().size());
    }

    @Test
    public void testCloseShutsDownAllThreadPools() throws Exception {
        MappIntelligenceTracking tracking = this.newFileTracking();
        tracking.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.test")));

        Map<Integer, ThreadPoolExecutor> executors = this.getExecutors(tracking);
        assertFalse(executors.isEmpty());
        for (ThreadPoolExecutor executor : executors.values()) {
            assertFalse(executor.isShutdown());
        }

        tracking.close();

        for (ThreadPoolExecutor executor : executors.values()) {
            assertTrue(executor.isShutdown());
            assertTrue(executor.isTerminated());
        }
    }

    @Test
    public void testQueueThreadsAreDaemonThreads() throws Exception {
        MappIntelligenceTracking tracking = this.newFileTracking();
        tracking.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.test")));
        tracking.flush();

        Thread.sleep(500);

        // a non-daemon queue thread would keep the JVM from terminating
        boolean foundQueueThread = false;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("mapp-intelligence-")) {
                foundQueueThread = true;
                assertTrue("thread " + thread.getName() + " is not a daemon thread", thread.isDaemon());
            }
        }
        assertTrue("no queue thread was started at all", foundQueueThread);

        tracking.close();
    }

    @Test
    public void testTryWithResources() throws Exception {
        try (MappIntelligenceTracking tracking = this.newFileTracking()) {
            assertTrue(tracking.track((new MappIntelligenceDataMap())
                .page(new MappIntelligencePage("en.page.test"))
            ) > -1);
        }

        assertEquals(1, this.writtenLines().size());
    }

    @Test
    public void testSharedUsageIsReported() throws Exception {
        final MappIntelligenceTracking tracking = this.newFileTracking();

        tracking.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.thread1")));
        assertFalse(this.outContent.toString().contains("is used by more than one thread"));

        // a second thread tracking through the same instance is the misuse we want to surface
        Thread other = new Thread(() -> tracking.track((new MappIntelligenceDataMap())
            .page(new MappIntelligencePage("en.page.thread2"))
        ));
        other.start();
        other.join();

        assertTrue(this.outContent.toString().contains("is used by more than one thread"));

        tracking.close();
    }

    @Test
    public void testSingleThreadedUsageIsNotReported() throws Exception {
        MappIntelligenceTracking tracking = this.newFileTracking();

        for (int i = 0; i < 5; i++) {
            tracking.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page." + i)));
        }
        tracking.close();

        assertFalse(this.outContent.toString().contains("is used by more than one thread"));
    }

    @Test
    public void testConcurrentGetUserIdCookieYieldsExactlyOneEverId() throws Exception {
        final int threads = 16;
        final MappIntelligenceTracking tracking = this.newFileTracking();

        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threads);
        final List<com.mapp.intelligence.tracking.MappIntelligenceCookie> cookies =
            java.util.Collections.synchronizedList(new ArrayList<com.mapp.intelligence.tracking.MappIntelligenceCookie>());

        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                try {
                    start.await();
                    com.mapp.intelligence.tracking.MappIntelligenceCookie cookie = tracking.getUserIdCookie(
                        com.mapp.intelligence.tracking.MappIntelligence.SMART,
                        com.mapp.intelligence.tracking.MappIntelligence.CLIENT_SIDE_COOKIE
                    );
                    if (cookie != null) {
                        cookies.add(cookie);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            worker.start();
        }

        start.countDown();
        assertTrue(done.await(30, java.util.concurrent.TimeUnit.SECONDS));

        // exactly one caller may create the ever ID - otherwise one of them gets a cookie whose
        // value does not match the "eid" that is actually sent
        assertEquals("more than one thread created an ever ID: " + cookies, 1, cookies.size());

        tracking.track((new MappIntelligenceDataMap()).page(new MappIntelligencePage("en.page.test")));
        tracking.close();

        String everIdFromCookie = cookies.get(0).getValue();
        List<String> written = this.writtenLines();
        assertEquals(1, written.size());
        assertTrue(
            "the request must carry the same ever ID as the cookie: " + written.get(0),
            written.get(0).contains("eid=" + everIdFromCookie)
        );
    }
}
