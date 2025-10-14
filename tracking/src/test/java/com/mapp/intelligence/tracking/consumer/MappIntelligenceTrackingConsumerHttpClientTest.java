package com.mapp.intelligence.tracking.consumer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import org.junit.*;

import static org.junit.Assert.*;

public class MappIntelligenceTrackingConsumerHttpClientTest {
    private static final List<String> contentMaxBatchSize = new ArrayList<>();
    private static final List<String> maxPayloadSize = new ArrayList<>();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeClass
    public static void setUpTestData() {
        String longText = MappIntelligenceUnitUtil.getLongText();

        for (int i = 0; i < 11 * 1000; i++) {
            contentMaxBatchSize.add("wt?p=300,0");
        }

        for (int i = 0; i < 9 * 1000; i++) {
            maxPayloadSize.add("wt?p=300,0&cp1=" + longText);
        }
    }

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        this.outContent.reset();
        System.setOut(this.originalOut);
    }

    @Test
    public void testNewConsumerHttpClient() {
        Map<String, Object> config = new HashMap<>();
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        new MappIntelligenceConsumerHttpClient(config);

        assertTrue(this.outContent.toString().trim().isEmpty());
    }

    @Test
    public void testBatchRequestResponding200ViaHttps() {
        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "123451234512345");
        config.put("trackDomain", "analytics01.wt-eu02.net");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertTrue(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/123451234512345/batch (1 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 200"));
    }

    @Test
    public void testBatchRequestIOException() {
        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "123451234512345");
        config.put("trackDomain", "test-batch-request.webtrekk-tracking-test.net");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertFalse(consumer.sendBatch(data));
        assertTrue(this.outContent.toString().contains("UnknownHostException"));
    }

    @Test
    public void testBatchRequestConnectionTimedOut() {
        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "123451234512345");
        config.put("trackDomain", "www.google.com:81");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        config.put("connectionTimeout", 3 * 1000);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertFalse(consumer.sendBatch(data));
        // java.net.SocketTimeoutException (connect timed out)
        assertTrue(this.outContent.toString().contains("SocketTimeoutException"));
    }

    @Test
    public void testBatchRequestResponding200ViaHttp() {
        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "123451234512345");
        config.put("trackDomain", "analytics01.wt-eu02.net");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        config.put("forceSSL", false);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertTrue(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Send batch data to http://analytics01.wt-eu02.net/123451234512345/batch (1 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 200"));
    }

    @Test
    public void testBatchRequestResponding404() {
        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "111111111111111");
        config.put("trackDomain", "analytics01.wt-eu02.net");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertFalse(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Send batch data to https://analytics01.wt-eu02.net/111111111111111/batch (1 req.)"));
        assertTrue(this.outContent.toString().contains("Batch request responding the status code 404"));
        assertTrue(this.outContent.toString().contains("HTTP Status 404"));
    }

    @Test
    public void testMaxBatchSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        assertFalse(consumer.sendBatch(contentMaxBatchSize));
        assertTrue(this.outContent.toString().contains("Batch size is larger than 10000 req. (11000 req.)"));
    }

    @Test
    public void testMaxPayloadSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);

        assertFalse(consumer.sendBatch(maxPayloadSize));
        assertTrue(this.outContent.toString().contains("Payload size is larger than 24MB (34.7MB)"));
    }

    @Test
    public void testReuseExistingConnection() throws InterruptedException {
        List<String> handshakeSessionId = new ArrayList<>();

        Map<String, Object> config = new HashMap<>();
        config.put("trackId", "123451234512345");
        config.put("trackDomain", "analytics01.wt-eu02.net");
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        config.put("forceSSL", true);
        MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(config);
        consumer.setOnSessionId((byte[] id) -> handshakeSessionId.add(MappIntelligenceUnitUtil.bytesToHex(id)));

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("wt?p=300," + i + "&nc=1");
        }

        assertTrue(this.outContent.toString(), consumer.sendBatch(data));
        assertNotEquals(String.format("%s | %s", null, handshakeSessionId.get(0)), null, handshakeSessionId.get(0));

        MappIntelligenceUnitUtil.await(2 * 1000);
        assertTrue(this.outContent.toString(), consumer.sendBatch(data));
        assertEquals(String.format("%s | %s", handshakeSessionId.get(0), handshakeSessionId.get(1)), handshakeSessionId.get(0), handshakeSessionId.get(1));

        MappIntelligenceUnitUtil.await(10 * 1000);
        assertTrue(this.outContent.toString(), consumer.sendBatch(data));
        assertEquals(String.format("%s | %s", handshakeSessionId.get(1), handshakeSessionId.get(2)), handshakeSessionId.get(1), handshakeSessionId.get(2));

        MappIntelligenceUnitUtil.await(30 * 1000);
        assertTrue(this.outContent.toString(), consumer.sendBatch(data));
        assertEquals(String.format("%s | %s", handshakeSessionId.get(2), handshakeSessionId.get(3)), handshakeSessionId.get(2), handshakeSessionId.get(3));

        MappIntelligenceUnitUtil.await(60 * 1000);
        assertTrue(this.outContent.toString(), consumer.sendBatch(data));
        assertEquals(String.format("%s | %s", handshakeSessionId.get(3), handshakeSessionId.get(4)), handshakeSessionId.get(3), handshakeSessionId.get(4));
    }
}
