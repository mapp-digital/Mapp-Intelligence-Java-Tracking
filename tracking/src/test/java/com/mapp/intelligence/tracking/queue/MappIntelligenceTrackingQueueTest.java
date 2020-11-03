package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.util.URLString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=300,0.*"));
    }

    @Test
    public void testAddEmptyStringTrackingData() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(0, requests.size());
    }

    @Test
    public void testAddEmptyMapTrackingData() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
    }

    @Test
    public void testMaxBatchSize1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setMaxBatchSize(10);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        for (int i = 0; i < 15; i++) {
            queue.add((new MappIntelligenceParameterMap()).add("pn", i + "").build());
        }

        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 10 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 11 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 12 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 13 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 14 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 15 req."));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(requests.get(i).matches("^wt\\?p=600," + i + ",,,,,[0-9]{13},0,,.*"));
        }
    }

    @Test
    public void testMaxBatchSize2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setMaxBatchSize(10);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        for (int i = 0; i < 15; i++) {
            queue.add("wt?p=300," + i);
        }

        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 10 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 11 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 12 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 13 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 14 req."));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 15 req."));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(15, requests.size());
        for (int i = 0; i < 15; i++) {
            assertTrue(requests.get(i).contains("wt?p=300," + i));
        }
    }

    @Test
    public void testWithUserAgent() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setUserAgent(userAgent);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-UA=" + URLString.encode(userAgent).replaceAll("(\\.)", "\\\\$0")));
    }

    @Test
    public void testWithUserAgent2() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setUserAgent(userAgent);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-UA=" + URLString.encode(userAgent).replaceAll("(\\.)", "\\\\$0")));
    }

    @Test
    public void testWithUserAgent3() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setUserAgent(userAgent);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0&X-WT-UA=test");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-UA=test$"));
    }

    @Test
    public void testWithRemoteAddr() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRemoteAddress(remoteAddr);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-IP=" + URLString.encode(remoteAddr).replaceAll("(\\.)", "\\\\$0") + "$"));
    }

    @Test
    public void testWithRemoteAddr2() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRemoteAddress(remoteAddr);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-IP=" + URLString.encode(remoteAddr).replaceAll("(\\.)", "\\\\$0") + "$"));
    }

    @Test
    public void testWithRemoteAddr3() {
        String remoteAddr = "127.0.0.1";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRemoteAddress(remoteAddr);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0&X-WT-IP=127.0.0.20");

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=300,0&.+"));
        assertTrue(requests.get(0).matches(".+&X-WT-IP=127\\.0\\.0\\.20.*"));
    }

    @Test
    public void testWithRequestURI() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRequestURL(requestURL);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testWithSmartPixelEverId() {
        String smartPixelEverId = "2157070685656224066";
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wtstp_eid", smartPixelEverId)
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches(".+&eid=" + smartPixelEverId + ".*"));
    }

    @Test
    public void testWithTrackServerEverId() {
        String trackServerEverId = "6157070685656224066";
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wteid_111111111111111", trackServerEverId)
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches(".+&eid=" + trackServerEverId + ".*"));
    }

    @Test
    public void testWithoutOldPixelEverId() {
        String oldPixelEverIdCookie = ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wt3_eid", oldPixelEverIdCookie)
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertFalse(requests.get(0).matches(".+&eid=.+"));
    }

    @Test
    public void testWithOldPixelEverId() {
        String oldPixelEverId = "2155991359353080227";
        String oldPixelEverIdCookie = ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";111111111111111|" + oldPixelEverId + "#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wt3_eid", oldPixelEverIdCookie)
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches(".+&eid=" + oldPixelEverId + ".*"));
    }

    @Test
    public void testDefaultPageName() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRequestURL(requestURL);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testDefaultPageNameWithoutParams1() {
        String requestURL = "https://sub.domain.tld/path/to/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setRequestURL(requestURL)
                .addUseParamsForDefaultPageName("aa")
                .addUseParamsForDefaultPageName("bb")
                .addUseParamsForDefaultPageName("cc");

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,sub\\.domain\\.tld%2Fpath%2Fto%2Fpage\\.html,,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&pu=" + URLString.encode(requestURL).replaceAll("(\\.)", "\\\\$0") + ".*"));
    }

    @Test
    public void testDefaultPageNameWithoutParams2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .addUseParamsForDefaultPageName("aa")
                .addUseParamsForDefaultPageName("bb")
                .addUseParamsForDefaultPageName("cc");

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
        assertFalse(requests.get(0).matches(".+&pu=.+"));
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
                .addUseParamsForDefaultPageName("cc");

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600," + pageNameRegExp + ",,,,,[0-9]{13},0,,&.+"));
        assertTrue(requests.get(0).matches(".+&pu=https%3A%2F%2F" + pageNameRegExp + ".*"));
    }

    @Test
    public void testEmptyReferrer() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,.*"));
    }

    @Test
    public void testReferrerNotEqualsOwnDomain() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testInvalidReferrer1() {
        String referrerURL = "foo.bar";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL)
                .setDomain(Collections.singletonList("sub.domain.tld"));

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testInvalidReferrer2() {
        String referrerURL = "foo.bar";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL);

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain1() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL)
                .setDomain(Collections.singletonList("sub.domain.tld"));

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain2() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL)
                .setDomain(Collections.singletonList(".+\\.domain\\.tld"));

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13},1,,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain3() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL)
                .setDomain(Collections.singletonList("[a-z]{3}\\.domain\\.tld)"));

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testReferrerEqualsOwnDomain4() {
        String referrerURL = "https://sub.domain.tld/path/to/previous/page.html";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setReferrerURL(referrerURL)
                .setDomain(Collections.singletonList("subsub.domain.tld"));

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add(new HashMap<>());

        List<String> requests = MappIntelligenceUnitUtil.getQueue(queue);
        assertEquals(1, requests.size());
        assertTrue(requests.get(0).matches("^wt\\?p=600,0,,,,,[0-9]{13}," + URLString.encode(referrerURL).replaceAll("(\\.)", "\\\\$0") + ",,.*"));
    }

    @Test
    public void testFlushEmptyQueue() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        assertTrue(queue.flush());
    }

    @Test
    public void testUndefinedConsumerType() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
                .setConsumerType("PRINT")
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        assertFalse(queue.flush());
    }

    @Test
    public void testFlushEmptyQueueWithDebug() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());

        assertTrue(queue.flush());
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 0 req."));
        assertTrue(this.outContent.toString().contains("MappIntelligenceQueue is empty"));
    }

    @Test
    public void testFlushQueueFailed1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        assertFalse(queue.flush());
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 1 req."));
        assertTrue(this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString().contains("HTTP Status 404"));
        assertTrue(this.outContent.toString().contains("Batch request failed!"));
        assertTrue(this.outContent.toString().contains("Batch of 1 req. sent, current queue size is 1 req."));
    }

    @Test
    public void testFlushHTTPQueueFailed2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");

        Thread.currentThread().interrupt();
        assertFalse(queue.flush());
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 1 req."));
        assertTrue(this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString().contains("HTTP Status 404"));
        assertTrue(this.outContent.toString().contains("Batch request failed!"));
        assertTrue(this.outContent.toString().contains("Batch of 1 req. sent, current queue size is 1 req."));
    }

    @Test
    public void testFlushHTTPQueueSuccess() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");

        assertTrue(queue.flush());
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 5 req."));
        assertTrue(this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/123451234512345/batch (5 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 200"));
        assertTrue(this.outContent.toString().contains("Batch of 5 req. sent, current queue size is 0 req."));
        assertTrue(this.outContent.toString().contains("MappIntelligenceQueue is empty"));
    }

    @Test
    public void testFlushFileQueueSuccess() {
        String tempFilePath = System.getProperty("user.dir") + "/src/main/resources/";
        String tempFilePrefix = "mapp_intelligence_test";

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setFilePath(tempFilePath)
            .setFilePrefix(tempFilePrefix)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,0");

        queue.flush();
        assertTrue(this.outContent.toString().contains("Create new file mapp_intelligence_test"));
        assertTrue(this.outContent.toString().contains("Add the following request to queue (1 req.)"));
        assertTrue(this.outContent.toString().contains("Sent batch requests, current queue size is 1 req."));
        assertTrue(this.outContent.toString().contains("Write batch data in mapp_intelligence_test"));
        assertTrue(this.outContent.toString().contains("Batch of 1 req. sent, current queue size is 0 req."));
        assertTrue(this.outContent.toString().contains("MappIntelligenceQueue is empty"));

        MappIntelligenceUnitUtil.deleteFiles(tempFilePath, tempFilePrefix, ".tmp");
        MappIntelligenceUnitUtil.deleteFiles(tempFilePath, tempFilePrefix, ".log");
    }

    @Test
    public void testCustomConsumer() {
        class TestCustomPrintConsumer implements MappIntelligenceConsumer {
            public boolean sendBatch(List<String> batchContent) {
                for (String request : batchContent) {
                    System.out.println(request);
                }

                return true;
            }
        }

        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
                .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
                .setConsumer(new TestCustomPrintConsumer())
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        MappIntelligenceQueue queue = new MappIntelligenceQueue(mappIntelligenceConfig.build());
        queue.add("wt?p=300,0");
        queue.add("wt?p=300,1");
        queue.add("wt?p=300,2");
        queue.add("wt?p=300,3");
        queue.add("wt?p=300,4");

        assertTrue(queue.flush());
        assertTrue(this.outContent.toString().contains("wt?p=300,0"));
        assertTrue(this.outContent.toString().contains("wt?p=300,1"));
        assertTrue(this.outContent.toString().contains("wt?p=300,2"));
        assertTrue(this.outContent.toString().contains("wt?p=300,3"));
        assertTrue(this.outContent.toString().contains("wt?p=300,4"));
    }
}
