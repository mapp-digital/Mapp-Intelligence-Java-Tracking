package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.data.MappIntelligenceAction;
import com.mapp.intelligence.tracking.data.MappIntelligenceCampaign;
import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceEngage;
import com.mapp.intelligence.tracking.data.MappIntelligencePage;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceProduct;
import com.mapp.intelligence.tracking.data.MappIntelligenceProductCollection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Invalid input - null values, malformed config files, immutable lists - must not throw.
 */
public class MappIntelligenceRobustnessTest {
    /**
     * @param content Content of the config file
     *
     * @return Path of a temporary properties file
     */
    private String writeConfigFile(String content) throws IOException {
        File file = File.createTempFile("mapp_intelligence_config", ".properties");
        file.deleteOnExit();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }

        return file.getAbsolutePath();
    }

    @Test
    public void testNullInStringSettersCountsAsNotSet() {
        Map<String, String> page = (new MappIntelligencePage())
            .setName(null)
            .setSearch(null)
            .setParameter(1, null)
            .setCategory(2, null)
            .getQueryParameter();
        assertTrue(page.isEmpty());

        Map<String, String> action = (new MappIntelligenceAction()).setName(null).getQueryParameter();
        assertTrue(action.isEmpty());
    }

    @Test
    public void testNullIsIgnoredWhereItWasDereferenced() {
        MappIntelligenceProduct product = (new MappIntelligenceProduct()).setId("p1").setStatus(null);
        assertEquals("p1", product.getQueryParameter().get("ba"));

        MappIntelligenceEngage engage = (new MappIntelligenceEngage()).setAttributes(null).addAttributes(null);
        assertTrue(engage.getQueryParameter().isEmpty());

        MappIntelligenceProductCollection products = (new MappIntelligenceProductCollection()).add(null);
        assertTrue(products.build().isEmpty());

        MappIntelligenceParameterMap parameters = (new MappIntelligenceParameterMap()).add(null, "a").add("b", null);
        assertTrue(parameters.build().isEmpty());

        assertEquals(-1, MappIntelligenceLogLevel.getValue(null));
        assertEquals(MappIntelligenceLogLevel.ERROR, (int) (new MappIntelligenceConfig()).setLogLevel((String) null).build().get("logLevel"));
    }

    @Test
    public void testCampaignWithNullIdAndNullMediaCode() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL("https://shop.example/page?wt_mc=newsletter");
        mic.setRequestQueues(0);

        try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(mic)) {
            tracking.track((new MappIntelligenceDataMap()).campaign((new MappIntelligenceCampaign()).setId(null)));
            tracking.track((new MappIntelligenceDataMap()).campaign((new MappIntelligenceCampaign()).setMediaCode(null)));

            Deque<String> requests = MappIntelligenceUnitUtil.getQueue(tracking, 0);
            assertEquals(2, requests.size());
            assertTrue(requests.peekFirst().contains("mc=wt_mc%253Dnewsletter"));
            assertFalse(requests.peekLast().contains("mc="));
        }
    }

    @Test
    public void testMissingConfigFileUsesDefaults() {
        Map<String, Object> config = (new MappIntelligenceConfig((String) null)).build();

        assertEquals(MappIntelligenceLogLevel.ERROR, (int) config.get("logLevel"));
    }

    @Test
    public void testIntegerLargerThanMaxValueUsesDefault() throws IOException {
        String path = this.writeConfigFile("consumer.maxAttempt=99999999999\nconsumer.maxBatchSize=10\n");

        Map<String, Object> config = (new MappIntelligenceConfig(path)).build();

        assertEquals(1, (int) config.get("maxAttempt"));
        assertEquals(10, (int) config.get("maxBatchSize"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testListsFromConfigFileAreTrimmedAndModifiable() throws IOException {
        String path = this.writeConfigFile("tracking.domain=a.example, b.example,,\ntracking.containsInclude=foo;;bar; \n");

        MappIntelligenceConfig mic = new MappIntelligenceConfig(path);
        mic.addDomain("c.example");
        Map<String, Object> config = mic.build();

        assertEquals(java.util.Arrays.asList("a.example", "b.example", "c.example"), config.get("domain"));
        assertEquals(java.util.Arrays.asList("foo", "bar"), config.get("containsInclude"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testImmutableListsAreAccepted() {
        List<String> empty = Collections.emptyList();
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://first.example/page")
            .setDomain(empty)
            .setContainsInclude(Collections.singletonList("first"));

        // an immutable list must neither break build() nor the add...() methods
        assertEquals(Collections.singletonList("first.example"), mic.build().get("domain"));
        mic.addContainsInclude("second");

        // a reused config derives the own domain from the current request URL, not from the first one
        mic.setRequestURL("https://second.example/page");
        assertEquals(Collections.singletonList("second.example"), mic.build().get("domain"));
    }

    @Test
    public void testIncludeFilterIsResetWhenFiltersAreRemoved() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setRequestURL("https://shop.example/page")
            .setContainsInclude(Collections.singletonList("checkout"));
        assertTrue((boolean) mic.build().get("deactivateByInAndExclude"));

        mic.setContainsInclude(Collections.<String>emptyList());
        assertFalse((boolean) mic.build().get("deactivateByInAndExclude"));
    }

    @Test
    public void testInterfaceSupportsTryWithResources() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        // compiles only because MappIntelligence is AutoCloseable and close() throws no checked exception
        try (MappIntelligence tracking = new MappIntelligenceTracking(mic)) {
            assertTrue(tracking instanceof MappIntelligenceTracking);
        }
    }

    @Test
    public void testUserIdCookieWithNullArguments() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        // null context counts as client side, null pixel version as unknown: no cookie, no exception
        try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(mic)) {
            assertEquals(null, tracking.getUserIdCookie(null, null));
        }

        try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(mic)) {
            assertEquals("wtstp_eid", tracking.getUserIdCookie(MappIntelligence.SMART, null).getName());
        }
    }
}
