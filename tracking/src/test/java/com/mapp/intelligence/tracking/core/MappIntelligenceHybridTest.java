package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.*;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class MappIntelligenceHybridTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final String tempFilePath = System.getProperty("java.io.tmpdir") + "/";
    private final String tempFilePrefix = "MappIntelligenceRequests";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);

        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".log");
    }

    @Test
    public void testDeactivateTacking() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setDeactivate(true);
        mic.setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        mappIntelligence.track();
        mappIntelligence.flush();
        assertEquals(0, MappIntelligenceUnitUtil.getQueue(mappIntelligence, 0).size());
    }

    @Test
    public void testTrackWithoutRequestURL() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig();
        mic.setRequestQueues(0);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        mappIntelligence.track();
        mappIntelligence.flush();
        assertEquals(0, MappIntelligenceUnitUtil.getQueue(mappIntelligence, 0).size());
    }

    @Test
    public void testTrackWithEmptyQueryParameter1() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld/pix");
        mic.setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        mappIntelligence.track();
        mappIntelligence.flush();
        assertEquals(0, MappIntelligenceUnitUtil.getQueue(mappIntelligence, 0).size());
    }

    @Test
    public void testTrackWithEmptyQueryParameter2() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld/pix?");
        mic.setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        mappIntelligence.track();
        mappIntelligence.flush();
        assertEquals(0, MappIntelligenceUnitUtil.getQueue(mappIntelligence, 0).size());
    }

    @Test
    public void testTrackWithQueryParameter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld/pix?foo=bar&test=1%202%203")
            .setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);
        mappIntelligence.setOnFlushComplete((success, queueId) -> latch.countDown());
        mappIntelligence.track();
        mappIntelligence.flush();
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertTrue(
            MappIntelligenceUnitUtil
                .getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
                .contains("wt?foo=bar&test=1%202%203")
        );
    }

    @Test
    public void testTrackWithQueryParameterAndHash() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld/pix?foo=bar&test=1%202%203#abc")
            .setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);
        mappIntelligence.setOnFlushComplete((success, queueId) -> latch.countDown());
        mappIntelligence.track();
        mappIntelligence.flush();
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertTrue(
            MappIntelligenceUnitUtil
                .getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
                .contains("wt?foo=bar&test=1%202%203")
        );
    }

    @Test
    public void testTrackWithRequestURL() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mic = new MappIntelligenceConfig();
        mic.setRequestQueues(0);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);
        mappIntelligence.setOnFlushComplete((success, queueId) -> latch.countDown());
        mappIntelligence.track("https://sub.domain.tld/pix?foo=bar&test=1%202%203");
        mappIntelligence.flush();
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertTrue(
            MappIntelligenceUnitUtil
                .getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
                .contains("wt?foo=bar&test=1%202%203")
        );
    }

    @Test
    public void testTrackWithNullRequestURL() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig();
        mic.setRequestQueues(0);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        mappIntelligence.track(null);
        mappIntelligence.flush();
        assertEquals(0, MappIntelligenceUnitUtil.getQueue(mappIntelligence, 0).size());
    }

    @Test
    public void testImageResponseAsString() {
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(new MappIntelligenceConfig());

        assertTrue(mappIntelligence.getResponseAsString().contains("GIF89"));
    }

    @Test
    public void testImageResponseAsBytes() {
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(new MappIntelligenceConfig());

        assertTrue(new String(mappIntelligence.getResponseAsBytes()).contains("GIF89"));
    }

    @Test
    public void testSetUserIdFailed1() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        assertNull(mappIntelligence.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testSetUserIdFailed2() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackId("111111111111111")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        assertNull(mappIntelligence.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testSetUserIdFailed3() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackDomain("analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceHybrid mappIntelligence = new MappIntelligenceHybrid(mic);

        assertNull(mappIntelligence.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testNullCookie() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setCookie(null);
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wtstp_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testServerSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.SERVER_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wteid_111111111111111", cookie.getName());
        assertTrue("/^8[0-9]{18}/", cookie.getValue().matches("^8[0-9]{18}"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("domain.tld", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewV4ClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^;111111111111111\\|8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testAppendV4ClientSideEverId() {
        String oldPixelEverIdCookie = "";
        oldPixelEverIdCookie += ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics.domain.tld"))
            .addCookie("wt3_eid", oldPixelEverIdCookie);
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(
            cookie.getValue().matches("^" + oldPixelEverIdCookie.replaceAll("(\\|)", "\\\\$0") + ";111111111111111\\|8[0-9]{18}$")
        );
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewV5ClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V5, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^;111111111111111\\|8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testAppendV5ClientSideEverId() {
        String oldPixelEverIdCookie = "";
        oldPixelEverIdCookie += ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics.domain.tld"))
            .addCookie("wt3_eid", oldPixelEverIdCookie);
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V5, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(
            cookie.getValue().matches("^" + oldPixelEverIdCookie.replaceAll("(\\|)", "\\\\$0") + ";111111111111111\\|8[0-9]{18}$")
        );
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewSmartPixelClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wtstp_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
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

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(new TestCustomPrintConsumer())
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL("https://sub.domain.tld/pix?foo=bar&test=1%202%203#abc")
            .setRequestQueues(0);

        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);
        mappIntelligenceTracking.setOnFlushComplete((success, queueId) -> latch.countDown());

        mappIntelligenceTracking.track();
        mappIntelligenceTracking.flush();
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?foo=bar&test=1%202%203"));
    }

    @Test
    public void testCustomConsumerWithRequestInterval() throws InterruptedException {
        class TestCustomPrintConsumer implements MappIntelligenceConsumer {
            public boolean sendBatch(List<String> batchContent) {
                for (String request : batchContent) {
                    System.out.println(request);
                }

                return true;
            }
        }

        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("123451234512345", "analytics01.wt-eu02.net"))
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(new TestCustomPrintConsumer())
            .setRequestInterval(1)
            .setRequestQueues(0)
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL("https://sub.domain.tld/pix?foo=bar&test=1%202%203#abc");

        MappIntelligenceHybrid mappIntelligenceTracking = new MappIntelligenceHybrid(mic);
        mappIntelligenceTracking.setOnFlushComplete((success, queueId) -> latch.countDown());

        mappIntelligenceTracking.track();
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertTrue(this.outContent.toString(), this.outContent.toString().contains("wt?foo=bar&test=1%202%203"));
    }
}
