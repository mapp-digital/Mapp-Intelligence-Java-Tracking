package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.util.URLString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class MappIntelligenceTrackingQueueTest {
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

    @Test
    public void testAddTrackingRequest() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0.*"));
    }

    @Test
    public void testAddEmptyStringTrackingData() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(0, requests.size());
    }

    @Test
    public void testAddEmptyMapTrackingData() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
    }

    @Test
    public void testMaxBatchSizeFlush1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(10)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 15; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (10 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed!"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 10 req. sent, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).matches("^wt\\?p=600," + i + ",,,,,[0-9]{13},0,,.*"));
        }
    }

    @Test
    public void testMaxBatchSizeFlush2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(10)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 15; i++) {
            queue.add("wt?p=300," + i);
        }

        queue.flush();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (10 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 10 req. sent, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).contains("wt?p=300," + i));
        }
    }

    @Test
    public void testMaxBatchSizeFlush3() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(10)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 15; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        queue.flush();
        MappIntelligenceUnitUtil.await(5);

        for (int i = 15; i < 30; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (10 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 10 req. sent, current queue -0- size is"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(30, requests.size());
        for (int i = 0; i < 30; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).matches("^wt\\?p=600," + i + ",,,,,[0-9]{13},0,,.*"));
        }
    }

    @Test
    public void testMaxBatchSizeFlush4() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(5)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 20; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        for (int i = 0; i < 5; i++) {
            queue.flush();
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));

        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 20"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (5 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 5 req. sent, current queue -0- size is 20 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Task rejected, queue -0- must be processed"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(20, requests.size());
        for (int i = 0; i < 20; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).matches("^wt\\?p=600," + i + ",,,,,[0-9]{13},0,,.*"));
        }
    }

    @Test
    public void testMaxBatchSizeRequestInterval1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(10)
            .setRequestInterval(1)
            .setRequestInterval(-1)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 15; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (10 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 10 req. sent, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).matches("^wt\\?p=600," + i + ",,,,,[0-9]{13},0,,.*"));
        }
    }

    @Test
    public void testMaxBatchSizeRequestInterval2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxBatchSize(10)
            .setRequestInterval(3)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 15; i++) {
            queue.add("wt?p=300," + i);
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (10 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 10 req. sent, current queue -0- size is 15 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(Objects.requireNonNull(requests.poll()).contains("wt?p=300," + i));
        }
    }

    @Test
    public void testWithUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setUserAgent(userAgent)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-UA=" + URLString.encode(userAgent).replaceAll("(\\.)", "\\\\$0")));
    }

    @Test
    public void testWithUserAgent2() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setUserAgent(userAgent)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-UA=" + URLString.encode(userAgent).replaceAll("(\\.)", "\\\\$0")));
    }

    @Test
    public void testWithUserAgent3() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setUserAgent(userAgent)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0&X-WT-UA=test");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-UA=test$"));
    }

    @Test
    public void testWithClientHints() {
        String clientHintUserAgent = "%22Chromium%22%3Bv%3D%22112%22%2C%20%22Google%20Chrome%22%3Bv%3D%22112%22%2C%20%22Not%3AA-Brand%22%3Bv%3D%2299%20";
        String clientHintUserAgentFullVersionList = "%22Chromium%22%3Bv%3D%22110.0.5481.65%22%2C%20%22Not%20A%28Brand%22%3Bv%3D%2224.0.0.0%22%2C%20%22Google%20Chrome%22%3Bv%3D%22110.0.5481.65%22";
        String clientHintUserAgentMobile = "%3F1";
        String clientHintUserAgentModel = "%22SM-A715F%22";
        String clientHintUserAgentPlatform = "%22macOS%22";
        String clientHintUserAgentPlatformVersion = "%2213.0.0%22";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setClientHintUserAgent(clientHintUserAgent)
            .setClientHintUserAgentFullVersionList(clientHintUserAgentFullVersionList)
            .setClientHintUserAgentMobile(clientHintUserAgentMobile)
            .setClientHintUserAgentModel(clientHintUserAgentModel)
            .setClientHintUserAgentPlatform(clientHintUserAgentPlatform)
            .setClientHintUserAgentPlatformVersion(clientHintUserAgentPlatformVersion)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA=" + clientHintUserAgent));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-FULL-VERSION-LIST=" + clientHintUserAgentFullVersionList));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MODEL=" + clientHintUserAgentModel));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MOBILE=" + clientHintUserAgentMobile));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM=" + clientHintUserAgentPlatform));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM-VERSION=" + clientHintUserAgentPlatformVersion));
    }

    @Test
    public void testWithClientHints2() {
        String clientHintUserAgent = "%22Chromium%22%3Bv%3D%22112%22%2C%20%22Google%20Chrome%22%3Bv%3D%22112%22%2C%20%22Not%3AA-Brand%22%3Bv%3D%2299%20";
        String clientHintUserAgentFullVersionList = "%22Chromium%22%3Bv%3D%22110.0.5481.65%22%2C%20%22Not%20A%28Brand%22%3Bv%3D%2224.0.0.0%22%2C%20%22Google%20Chrome%22%3Bv%3D%22110.0.5481.65%22";
        String clientHintUserAgentMobile = "%3F1";
        String clientHintUserAgentModel = "%22SM-A715F%22";
        String clientHintUserAgentPlatform = "%22macOS%22";
        String clientHintUserAgentPlatformVersion = "%2213.0.0%22";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setClientHintUserAgent(clientHintUserAgent)
            .setClientHintUserAgentFullVersionList(clientHintUserAgentFullVersionList)
            .setClientHintUserAgentMobile(clientHintUserAgentMobile)
            .setClientHintUserAgentModel(clientHintUserAgentModel)
            .setClientHintUserAgentPlatform(clientHintUserAgentPlatform)
            .setClientHintUserAgentPlatformVersion(clientHintUserAgentPlatformVersion)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA=" + clientHintUserAgent));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-FULL-VERSION-LIST=" + clientHintUserAgentFullVersionList));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MODEL=" + clientHintUserAgentModel));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MOBILE=" + clientHintUserAgentMobile));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM=" + clientHintUserAgentPlatform));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM-VERSION=" + clientHintUserAgentPlatformVersion));
    }

    @Test
    public void testWithClientHints3() {
        String clientHintUserAgent = "%22Chromium%22%3Bv%3D%22112%22%2C%20%22Google%20Chrome%22%3Bv%3D%22112%22%2C%20%22Not%3AA-Brand%22%3Bv%3D%2299%20";
        String clientHintUserAgentFullVersionList = "%22Chromium%22%3Bv%3D%22110.0.5481.65%22%2C%20%22Not%20A%28Brand%22%3Bv%3D%2224.0.0.0%22%2C%20%22Google%20Chrome%22%3Bv%3D%22110.0.5481.65%22";
        String clientHintUserAgentMobile = "%3F1";
        String clientHintUserAgentModel = "%22SM-A715F%22";
        String clientHintUserAgentPlatform = "%22macOS%22";
        String clientHintUserAgentPlatformVersion = "%2213.0.0%22";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setClientHintUserAgent(clientHintUserAgent)
            .setClientHintUserAgentFullVersionList(clientHintUserAgentFullVersionList)
            .setClientHintUserAgentMobile(clientHintUserAgentMobile)
            .setClientHintUserAgentModel(clientHintUserAgentModel)
            .setClientHintUserAgentPlatform(clientHintUserAgentPlatform)
            .setClientHintUserAgentPlatformVersion(clientHintUserAgentPlatformVersion)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0&X-WT-SEC-CH-UA=sec-ch-ua&X-WT-SEC-CH-UA-FULL-VERSION-LIST=sec-ch-ua-full-version-list&X-WT-SEC-CH-UA-MODEL=sec-ch-ua-model&X-WT-SEC-CH-UA-MOBILE=sec-ch-ua-mobile&X-WT-SEC-CH-UA-PLATFORM=sec-ch-ua-platform&X-WT-SEC-CH-UA-PLATFORM-VERSION=sec-ch-ua-platform-version");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA=sec-ch-ua"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-FULL-VERSION-LIST=sec-ch-ua-full-version-list"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MODEL=sec-ch-ua-model"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-MOBILE=sec-ch-ua-mobile"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM=sec-ch-ua-platform"));
        assertTrue(requests.peek().contains("&X-WT-SEC-CH-UA-PLATFORM-VERSION=sec-ch-ua-platform-version"));
    }

    @Test
    public void testWithRemoteAddr() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRemoteAddress(remoteAddr)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-IP=" + URLString.encode(remoteAddr).replaceAll("(\\.)", "\\\\$0") + "$"));
    }

    @Test
    public void testWithRemoteAddr2() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRemoteAddress(remoteAddr)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-IP=" + URLString.encode(remoteAddr).replaceAll("(\\.)", "\\\\$0") + "$"));
    }

    @Test
    public void testWithRemoteAddr3() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRemoteAddress(remoteAddr)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0&X-WT-IP=127.0.0.20");

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=300,0&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&X-WT-IP=127\\.0\\.0\\.20.*"));
    }

    @Test
    public void testWithRequestURI() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestURL(requestURL)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testWithSmartPixelEverId() {
        String smartPixelEverId = "2157070685656224066";
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .addCookie("wtstp_eid", smartPixelEverId)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&eid=" + smartPixelEverId + ".*"));
    }

    @Test
    public void testWithTrackServerEverId() {
        String trackServerEverId = "6157070685656224066";
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .addCookie("wteid_111111111111111", trackServerEverId)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&eid=" + trackServerEverId + ".*"));
    }

    @Test
    public void testWithoutOldPixelEverId() {
        String oldPixelEverIdCookie = ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .addCookie("wt3_eid", oldPixelEverIdCookie)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertFalse(Objects.requireNonNull(requests.peek()).matches(".+&eid=.+"));
    }

    @Test
    public void testWithOldPixelEverId() {
        String oldPixelEverId = "2155991359353080227";
        String oldPixelEverIdCookie = ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";111111111111111|" + oldPixelEverId + "#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .addCookie("wt3_eid", oldPixelEverIdCookie)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&eid=" + oldPixelEverId + ".*"));
    }

    @Test
    public void testDefaultPageName() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestURL(requestURL)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testDefaultPageNameWithoutParams1() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL(requestURL)
            .addUseParamsForDefaultPageName("aa")
            .addUseParamsForDefaultPageName("bb")
            .addUseParamsForDefaultPageName("cc")
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testDefaultPageNameWithoutParams2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .addUseParamsForDefaultPageName("aa")
            .addUseParamsForDefaultPageName("bb")
            .addUseParamsForDefaultPageName("cc")
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
        assertFalse(Objects.requireNonNull(requests.peek()).matches(".+&pu=.+"));
    }

    @Test
    public void testDefaultPageNameWithParams() {
        String requestURL = "https://sub.domain.tld/path/to/page.html?bb=value%20bb&cc=value%20cc";
        String pageNameRegExp = "sub\\.domain\\.tld" + URLString.encode("/path/to/page.html?bb=value bb&cc=value cc");

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL(requestURL)
            .addUseParamsForDefaultPageName("aa")
            .addUseParamsForDefaultPageName("bb")
            .addUseParamsForDefaultPageName("cc")
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600," + pageNameRegExp + ",,,,,[0-9]{13},0,,&.+"));
        assertTrue(Objects.requireNonNull(requests.peek()).matches(".+&pu=https%3A%2F%2F" + pageNameRegExp + ".*"));
    }

    @Test
    public void testEmptyReferrer() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
    }

    @Test
    public void testReferrerNotEqualsOwnDomain() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testInvalidReferrer1() {
        String referrerURL = "foo.bar";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setDomain(Collections.singletonList("sub.domain.tld"))
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testInvalidReferrer2() {
        String referrerURL = "foo.bar";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain1() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setDomain(Collections.singletonList("sub.domain.tld"))
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain2() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setDomain(Collections.singletonList(".+\\.domain\\.tld"))
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain3() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setDomain(Collections.singletonList("[a-z]{3}\\.domain\\.tld)"))
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain4() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setReferrerURL(referrerURL)
            .setDomain(Collections.singletonList("subsub.domain.tld"))
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        Deque<String> requests = queue.getQueue(0);
        assertEquals(1, requests.size());
        assertTrue(Objects.requireNonNull(requests.peek()).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testFlushEmptyQueue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.flush();
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Queue -0- is empty"));
    }

    @Test
    public void testUndefinedConsumerType() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType("PRINT")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("no consumer is available or an undefined consumer type"));
    }

    @Test
    public void testFlushEmptyQueueWithDebug() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 0 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Queue -0- is empty"));
    }

    @Test
    public void testFlushQueueFailed1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("HTTP Status 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed!"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 1 req. sent, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));
    }

    @Test
    public void testFlushHTTPQueueFailed2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());
        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("HTTP Status 404"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 1 req. sent, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/1)"));
    }

    @Test
    public void testFlushHTTPQueueFailed3() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "www.google.com:81"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setMaxAttempt(3)
            .setConnectionTimeout(3000)
            .setConnectionTimeout(-3000)
            .setReadTimeout(3000)
            .setReadTimeout(-3000)
            .setRequestQueues(0)
            .setRequestQueues(-1);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());
        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://www.google.com:81/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("SocketTimeoutException"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request for queue -0- failed"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 1 req. sent, current queue -0- size is 1 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (1/3)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (2/3)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Flush for queue -0- was not successful (3/3)"));
    }

    @Test
    public void testFlushHTTPQueueSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is 5 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/123451234512345/batch (5 req.)"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch request responding the status code 200"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 5 req. sent, current queue -0- size is 0 req."));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Queue -0- is empty"));
    }

    @Test
    public void testFlushFileQueueSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String tempFilePath = System.getProperty("user.dir") + "/src/main/resources/";
        String tempFilePrefix = "mapp_intelligence_test";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setFilePath(tempFilePath)
            .setFilePrefix(tempFilePrefix)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.add("wt?p=300,1");
        queue.add("wt?p=300,2");
        queue.add("wt?p=300,3");
        queue.add("wt?p=300,4");
        queue.add("wt?p=300,5");

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Create new file mapp_intelligence_test"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Add the following request to queue -0-"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Send batch requests, current queue -0- size is"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Write batch data in mapp_intelligence_test"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Batch of 1 req. sent, current queue -0- size is"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Queue -0- is empty"));

        MappIntelligenceUnitUtil.deleteFiles(tempFilePath, tempFilePrefix, ".tmp");
        MappIntelligenceUnitUtil.deleteFiles(tempFilePath, tempFilePrefix, ".log");
    }

    @Test
    public void testMaxBatchRequestBodySize() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.INFO)
            .setMaxBatchSize(500 * 1000)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        String longText = MappIntelligenceUnitUtil.getLongText();
        for (int i = 0; i < 10 * 1000; i++) {
            queue.add("wt?p=300,0&cp1=" + longText);
        }

        queue.flush();
        assertTrue(latch.await(60, TimeUnit.SECONDS));

        assertTrue(this.outContent.toString(), this.outContent.toString().contains("Maximum batch payload limit of 20MB for queue -0- reached"));

        Deque<String> requests = queue.getQueue(0);
        assertEquals(10 * 1000, requests.size());
    }

    @Test
    public void testCorrectQueueAssignment() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(20);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        for (int i = 0; i < 20; i++) {
            assertEquals(i, queue.add("wt?p=300,0&eid=215707068565622406" + i));
        }

        // negativ EverID
        assertEquals(14, queue.add("wt?p=300,0&eid=-215707068565622406"));

        // EverID isn't a number
        assertEquals(16, queue.add("wt?p=300,0&eid=foo@bar.com"));

        // EverID is empty
        assertEquals(20, queue.add("wt?p=300,0&eid="));
        assertEquals(20, queue.add("wt?p=300,0"));
    }

    @Test
    public void testCorrectQueueAssignmentViaUA() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:143.0) Gecko/20100101 Firefox/143.0")
            .setRequestQueues(20);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        assertEquals(0, queue.add("wt?p=300,0"));
    }

    @Test
    public void testCorrectQueueAssignmentViaCHUA() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setClientHintUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:143.0) Gecko/20100101 Firefox/143.0")
            .setRequestQueues(20);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        assertEquals(0, queue.add("wt?p=300,0"));
    }

    @Test
    public void testCorrectQueueAssignmentViaUAIP() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:143.0) Gecko/20100101 Firefox/143.0")
            .setRemoteAddress("192.168.5.70")
            .setRequestQueues(20);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        assertEquals(17, queue.add("wt?p=300,0"));
    }

    @Test
    public void testQueueSchedulerIsRunning() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        class TestCustomMockConsumer implements MappIntelligenceConsumer {
            public boolean sendBatch(List<String> batchContent) {
                for (String req : batchContent) {
                    assertTrue(req + " / " + counter.get(), req.contains("," + counter.getAndIncrement() + ","));
                }

                try {
                    MappIntelligenceUnitUtil.await(3000);
                } catch (InterruptedException e) {
                    // do nothing
                }

                return true;
            }
        }

        CountDownLatch latch = new CountDownLatch(2);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(new TestCustomMockConsumer())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                queue.add("wt?p=300," + (i > 0 ? i : "") + j + ",");
            }

            queue.flush();
            MappIntelligenceUnitUtil.await(200);
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertEquals(100, counter.get());
    }

    @Test
    public void testCustomConsumer() throws InterruptedException {
        class TestCustomPrintConsumer implements MappIntelligenceConsumer {
            public boolean sendBatch(List<String> batchContent) {
                for (String request : batchContent) {
                    System.out.println(request);
                }

                return true;
            }
        }

        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(new TestCustomPrintConsumer())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.setOnFlushComplete((success, queueId) -> latch.countDown());

        queue.add("wt?p=300,0");
        queue.add("wt?p=300,1");
        queue.add("wt?p=300,2");
        queue.add("wt?p=300,3");
        queue.add("wt?p=300,4");

        queue.flush();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?p=300,0"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?p=300,1"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?p=300,2"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?p=300,3"));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?p=300,4"));
    }

    @Test
    public void stressTestWithMultipleThreads() throws InterruptedException {
        MappIntelligenceConsumer consumerMock = mock(MappIntelligenceConsumer.class);
        when(consumerMock.sendBatch(anyList())).thenReturn(true);

        int threadCount = 20;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(consumerMock)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setRequestQueues(0);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                for (int i = 0; i < requestsPerThread; i++) {
                    queue.add(UUID.randomUUID().toString());
                }
                queue.flush();
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        MappIntelligenceUnitUtil.await(3000);

        verify(consumerMock, atLeast(1)).sendBatch(anyList());
    }
}
