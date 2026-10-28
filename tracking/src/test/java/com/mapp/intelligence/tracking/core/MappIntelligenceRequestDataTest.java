package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceSession;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Request data found in the review of the Node.js library: the map of the caller, the page URL, the query parameters
 * of the request URL, the server parameters of a hybrid request.
 */
public class MappIntelligenceRequestDataTest {
    /**
     * Records every request it receives.
     */
    private static class RecordingConsumer implements MappIntelligenceConsumer {
        final List<String> requests = Collections.synchronizedList(new ArrayList<>());

        @Override
        public boolean sendBatch(List<String> batchContent) {
            this.requests.addAll(batchContent);
            return true;
        }
    }

    /**
     * @param consumer Consumer
     *
     * @return Configuration with the consumer
     */
    private static MappIntelligenceConfig createConfig(RecordingConsumer consumer) {
        return new MappIntelligenceConfig("111111111111111", "analytics.domain.tld")
            .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
            .setConsumer(consumer);
    }

    /**
     * @param config Configuration
     * @param data Request data
     * @param consumer Consumer of the configuration
     *
     * @return The tracked request
     */
    private static String track(MappIntelligenceConfig config, MappIntelligenceParameterMap data, RecordingConsumer consumer) {
        try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(config)) {
            tracking.track(data);
        }

        return consumer.requests.get(consumer.requests.size() - 1);
    }

    @Test
    public void testTheParameterMapOfTheCallerIsNotChanged() {
        RecordingConsumer consumer = new RecordingConsumer();
        MappIntelligenceParameterMap data = new MappIntelligenceParameterMap().add(MappIntelligenceParameter.PAGE_NAME, "en.page");

        track(createConfig(consumer)
            .setRemoteAddress("1.2.3.4")
            .setUserAgent("UA-Alice")
            .setActivateAdvancedPermission(true)
            .setAdvancedPermissionCategory(3), data, consumer);

        Map<String, String> expected = new HashMap<>();
        expected.put(MappIntelligenceParameter.PAGE_NAME, "en.page");
        assertEquals(expected, data.build());

        // a second instance without user data sends the page name and nothing of the first user
        String request = track(createConfig(consumer), data, consumer);
        assertTrue(request, request.startsWith("wt?p=600,en.page,"));
        assertFalse(request, request.contains("1.2.3.4"));
        assertFalse(request, request.contains("pec="));
    }

    @Test
    public void testAPathWithDoubleSlashAndAUrlInTheQueryAreKept() {
        RecordingConsumer consumer = new RecordingConsumer();
        String request = track(createConfig(consumer).setRequestURL("https://sub.domain.tld/a//b?r=http://x.de/y"),
            new MappIntelligenceParameterMap(), consumer);

        assertTrue(request, request.startsWith("wt?p=600,sub.domain.tld%2Fa%2F%2Fb,"));
        assertTrue(request, request.contains("&pu=https%3A%2F%2Fsub.domain.tld%2Fa%2F%2Fb%3Fr%3Dhttp%3A%2F%2Fx.de%2Fy"));
    }

    @Test
    public void testThePageUrlHasTheProtocolOfTheRequest() {
        RecordingConsumer consumer = new RecordingConsumer();
        String request = track(createConfig(consumer).setRequestURL("http://sub.domain.tld/plain"),
            new MappIntelligenceParameterMap(), consumer);

        assertTrue(request, request.contains("&pu=http%3A%2F%2Fsub.domain.tld%2Fplain"));
    }

    @Test
    public void testTheQueryIsSplitBeforeItIsDecoded() {
        RecordingConsumer consumer = new RecordingConsumer();
        String request = track(createConfig(consumer)
            .setRequestURL("https://sub.domain.tld/page?q=a%26b&x=what%3F&id=1")
            .setUseParamsForDefaultPageName(Arrays.asList("q", "x", "id")), new MappIntelligenceParameterMap(), consumer);

        // q=a&b, x=what? and id=1 - before, the encoded "&" cut the value of q
        assertTrue(request, request.startsWith("wt?p=600,sub.domain.tld%2Fpage%3Fq%3Da%26b%26x%3Dwhat%3F%26id%3D1,"));
    }

    @Test
    public void testAParameterNameInAValueDoesNotSuppressTheEnrichment() {
        RecordingConsumer consumer = new RecordingConsumer();
        try (MappIntelligenceQueue queue = new MappIntelligenceQueue(createConfig(consumer)
            .setRemoteAddress("1.2.3.4")
            .setUserAgent("UA")
            .build())) {
            queue.add("wt?p=600,0&cs1=see%20X-WT-UA%20X-WT-IP");
            queue.add("wt?p=600,0&X-WT-IP=5.6.7.8");
        }

        assertEquals(2, consumer.requests.size());
        String withValue = consumer.requests.get(0).contains("cs1=") ? consumer.requests.get(0) : consumer.requests.get(1);
        String withIp = consumer.requests.get(0).contains("cs1=") ? consumer.requests.get(1) : consumer.requests.get(0);
        assertTrue(withValue, withValue.contains("X-WT-IP=1.2.3.4"));
        assertTrue(withValue, withValue.contains("X-WT-UA=UA"));
        assertFalse(withIp, withIp.contains("X-WT-IP=1.2.3.4"));
    }

    @Test
    public void testTheClientCannotSetTheParametersOfTheServer() {
        RecordingConsumer consumer = new RecordingConsumer();
        try (MappIntelligenceHybrid hybrid = new MappIntelligenceHybrid(createConfig(consumer)
            .setRemoteAddress("1.2.3.4")
            .setRequestURL("https://sub.domain.tld/pix?p=400,0&X-WT-IP=5.6.7.8&x-wt-ua=Fake&X-WT-SEC-CH-UA=Fake"))) {
            hybrid.track();
        }

        assertEquals(1, consumer.requests.size());
        String request = consumer.requests.get(0);
        assertTrue(request, request.contains("X-WT-IP=1.2.3.4"));
        assertFalse(request, request.contains("5.6.7.8"));
        assertFalse(request, request.contains("Fake"));
    }

    @Test
    public void testAnInvalidUrlDoesNotTrackThePreviousRequestAgain() {
        RecordingConsumer consumer = new RecordingConsumer();
        try (MappIntelligenceHybrid hybrid = new MappIntelligenceHybrid(createConfig(consumer)
            .setRequestURL("https://sub.domain.tld/pix?p=400,0"))) {
            hybrid.track();
            assertEquals(-1, hybrid.track("not a url"));
        }

        assertEquals(1, consumer.requests.size());
    }

    @Test
    public void testATemporarySessionIdWithoutValueSendsNoType() {
        assertTrue((new MappIntelligenceSession()).setTemporarySessionId("").getQueryParameter().isEmpty());
        assertTrue((new MappIntelligenceSession()).setTemporarySessionId(null).getQueryParameter().isEmpty());

        Map<String, String> session = (new MappIntelligenceSession()).setTemporarySessionId("abc").getQueryParameter();
        assertEquals("abc", session.get(MappIntelligenceParameter.TEMPORARY_SESSION_ID));
        assertEquals("2.0.0", session.get(MappIntelligenceParameter.TEMPORARY_SESSION_ID_TYPE));
    }
}
