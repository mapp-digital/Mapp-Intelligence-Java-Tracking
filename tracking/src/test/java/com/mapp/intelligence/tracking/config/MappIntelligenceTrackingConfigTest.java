package com.mapp.intelligence.tracking.config;

import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingConfigTest {
    @Test
    public void testDefaultConfig() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("", config.get("trackId"));
        assertEquals("", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(50, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 1000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(0, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(34, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testNullConfig() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(null, null);

        mappIntelligenceConfig.setTrackId(null)
            .setTrackDomain(null)
            .setUserAgent(null)
            .setRemoteAddress(null)
            .setReferrerURL(null)
            .setRequestURL(null)
            .setCookie(null).addCookie(null, null).addCookie("", null).addCookie(null, "")
            .setDomain(null).addDomain(null)
            .setLogger(null)
            .setConsumerType(null)
            .setConsumer(null)
            .setFilePath(null)
            .setFilePrefix(null)
            .setContainsInclude(null).addContainsInclude(null)
            .setContainsExclude(null).addContainsExclude(null)
            .setMatchesInclude(null).addMatchesInclude(null)
            .setMatchesExclude(null).addMatchesExclude(null)
            .setUseParamsForDefaultPageName(null).addUseParamsForDefaultPageName(null);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("", config.get("trackId"));
        assertEquals("", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(50, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 1000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(0, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(34, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testInvalidConfigFile() {
        String configFile = System.getProperty("user.dir") + "/src/main/resources/foo.bar";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("", config.get("trackId"));
        assertEquals("", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(50, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 1000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(0, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(34, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testPropertyConfigFile1() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config.properties";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("", config.get("trackId"));
        assertEquals("", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(50, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 1000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(0, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(34, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testPropertyConfigFile2() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config_test.properties";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("123451234512345", config.get("trackId"));
        assertEquals("analytics01.wt-eu02.net", config.get("trackDomain"));
        assertEquals(2, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.FILE, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(1, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 100, config.get("maxFileLines"));
        assertEquals(30 * 60 * 100, config.get("maxFileDuration"));
        assertEquals(24 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(3, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).endsWith("/"));
        assertEquals("MappIntelligenceRequests", config.get("filePrefix"));
        assertEquals(131, config.get("statistics"));
        assertEquals(2, ((List<String>) config.get("containsInclude")).size());
        assertEquals(1, ((List<String>) config.get("containsExclude")).size());
        assertEquals(2, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(1, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testOverwritePropertyConfigFile() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config.properties";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        mappIntelligenceConfig.setTrackId("111111111111111")
            .setTrackDomain("analytics01.wt-eu02.net")
            .setMaxAttempt(3)
            .setAttemptTimeout(200)
            .setMaxBatchSize(1000)
            .setMaxQueueSize(100000)
            .setMaxFileLines(300)
            .setMaxFileDuration(60000)
            .setMaxFileSize(12 * 1024 * 1024)
            .setForceSSL(false)
            .setUseParamsForDefaultPageName(new ArrayList<>())
            .addUseParamsForDefaultPageName("foo")
            .addUseParamsForDefaultPageName("bar");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("111111111111111", config.get("trackId"));
        assertEquals("analytics01.wt-eu02.net", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(3, config.get("maxAttempt"));
        assertEquals(200, config.get("attemptTimeout"));
        assertEquals(1000, config.get("maxBatchSize"));
        assertEquals(100000, config.get("maxQueueSize"));
        assertEquals(300, config.get("maxFileLines"));
        assertEquals(60000, config.get("maxFileDuration"));
        assertEquals(12 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(false, config.get("forceSSL"));
        assertEquals(2, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(33, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testXMLConfigFile1() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config.xml";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("", config.get("trackId"));
        assertEquals("", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(50, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 1000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(0, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(34, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testXMLConfigFile2() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config_test.xml";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("123451234512345", config.get("trackId"));
        assertEquals("analytics01.wt-eu02.net", config.get("trackDomain"));
        assertEquals(2, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.FILE, config.get("consumerType"));
        assertEquals(1, config.get("maxAttempt"));
        assertEquals(100, config.get("attemptTimeout"));
        assertEquals(1, config.get("maxBatchSize"));
        assertEquals(1000, config.get("maxQueueSize"));
        assertEquals(10 * 100, config.get("maxFileLines"));
        assertEquals(30 * 60 * 100, config.get("maxFileDuration"));
        assertEquals(24 * 1024, config.get("maxFileSize"));
        assertEquals(true, config.get("forceSSL"));
        assertEquals(3, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertEquals("", config.get("userAgent"));
        assertEquals("", config.get("remoteAddress"));
        assertEquals("", config.get("referrerURL"));
        assertNull(config.get("requestURL"));
        assertTrue(((String) config.get("filePath")).endsWith("/"));
        assertEquals("MappIntelligenceRequests", config.get("filePrefix"));
        assertEquals(131, config.get("statistics"));
        assertEquals(2, ((List<String>) config.get("containsInclude")).size());
        assertEquals(1, ((List<String>) config.get("containsExclude")).size());
        assertEquals(2, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(1, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testOverwriteXMLConfigFile() {
        String configFile = System.getProperty("user.dir") + "/src/test/resources/config.xml";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);
        mappIntelligenceConfig.setTrackId("111111111111111")
            .setTrackDomain("analytics01.wt-eu02.net")
            .setMaxAttempt(3)
            .setAttemptTimeout(200)
            .setMaxBatchSize(1000)
            .setMaxQueueSize(100000)
            .setMaxFileLines(300)
            .setMaxFileDuration(60000)
            .setMaxFileSize(12 * 1024 * 1024)
            .setForceSSL(false)
            .setUseParamsForDefaultPageName(new ArrayList<>())
            .addUseParamsForDefaultPageName("foo")
            .addUseParamsForDefaultPageName("bar");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("111111111111111", config.get("trackId"));
        assertEquals("analytics01.wt-eu02.net", config.get("trackDomain"));
        assertEquals(1, ((List<String>) config.get("domain")).size());
        assertNull(config.get("logger"));
        assertEquals(MappIntelligenceConsumerType.HTTP_CLIENT, config.get("consumerType"));
        assertEquals(3, config.get("maxAttempt"));
        assertEquals(200, config.get("attemptTimeout"));
        assertEquals(1000, config.get("maxBatchSize"));
        assertEquals(100000, config.get("maxQueueSize"));
        assertEquals(300, config.get("maxFileLines"));
        assertEquals(60000, config.get("maxFileDuration"));
        assertEquals(12 * 1024 * 1024, config.get("maxFileSize"));
        assertEquals(false, config.get("forceSSL"));
        assertEquals(2, ((List<String>) config.get("useParamsForDefaultPageName")).size());
        assertTrue(((String) config.get("filePath")).isEmpty());
        assertTrue(((String) config.get("filePrefix")).isEmpty());
        assertEquals(33, config.get("statistics"));
        assertEquals(0, ((List<String>) config.get("containsInclude")).size());
        assertEquals(0, ((List<String>) config.get("containsExclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesInclude")).size());
        assertEquals(0, ((List<String>) config.get("matchesExclude")).size());
    }

    @Test
    public void testConfigWithTrackIdAndTrackDomain() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        Map<String, Object> config = mappIntelligenceConfig.build();

        assertEquals("111111111111111", config.get("trackId"));
        assertEquals("analytics01.wt-eu02.net", config.get("trackDomain"));
    }

    @Test
    public void testConfigWithDomain() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setDomain(new ArrayList<>())
            .addDomain("foo.bar.com")
            .addDomain("www.mappIntelligence.com")
            .addDomain("sub.domain.tld");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("foo.bar.com", ((List<String>) config.get("domain")).get(0));
        assertEquals("www.mappIntelligence.com", ((List<String>) config.get("domain")).get(1));
        assertEquals("sub.domain.tld", ((List<String>) config.get("domain")).get(2));
    }

    @Test
    public void testConfigWithFile() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setConsumerType(MappIntelligenceConsumerType.FILE)
            .setFilePath("/dev/")
            .setFilePrefix("null")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger());

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("/dev/", config.get("filePath"));
        assertEquals("null", config.get("filePrefix"));
        assertEquals(MappIntelligenceConsumerType.FILE, config.get("consumerType"));
        assertEquals(1, config.get("maxBatchSize"));
        assertEquals(134, config.get("statistics"));
    }

    @Test
    public void testHeaderData() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0")
            .setRemoteAddress("127.0.0.1")
            .setReferrerURL("https://sub.domain.tld/path/to/previous/page.html")
            .setRequestURL("https://sub.domain.tld/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0", config.get("userAgent"));
        assertEquals("127.0.0.1", config.get("remoteAddress"));
        assertEquals("https://sub.domain.tld/path/to/previous/page.html", config.get("referrerURL"));
        assertEquals("sub.domain.tld", ((List<String>) config.get("domain")).get(0));
    }

    @Test
    public void testRequestURLInvalid() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setRequestURL("sub.domain.tld:8080/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("", ((List<String>) config.get("domain")).get(0));
    }

    @Test
    public void testRequestURLWith8080() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld:8080/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("sub.domain.tld:8080", ((List<String>) config.get("domain")).get(0));
    }

    @Test
    public void testRequestURLWith443() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld:443/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("sub.domain.tld", ((List<String>) config.get("domain")).get(0));
    }

    @Test
    public void testRequestURLWith80() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("sub.domain.tld", ((List<String>) config.get("domain")).get(0));
    }

    @Test
    public void testInvalidMaxAttempt1() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxAttempt(12);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(1, config.get("maxAttempt"));
    }

    @Test
    public void testInvalidMaxAttempt2() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxAttempt(-12);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(1, config.get("maxAttempt"));
    }

    @Test
    public void testInvalidAttemptTimeout1() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setAttemptTimeout(750);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(100, config.get("attemptTimeout"));
    }

    @Test
    public void testInvalidAttemptTimeout2() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setAttemptTimeout(-750);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(100, config.get("attemptTimeout"));
    }

    @Test
    public void testInvalidMaxFileLines1() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileLines(11000);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(10000, config.get("maxFileLines"));
    }

    @Test
    public void testInvalidMaxFileLines2() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileLines(-1000);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(10000, config.get("maxFileLines"));
    }

    @Test
    public void testInvalidMaxFileDuration1() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileDuration(31 * 60 * 1000);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
    }

    @Test
    public void testInvalidMaxFileDuration2() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileDuration(-30 * 1000);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
    }

    @Test
    public void testInvalidMaxFileSize1() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileSize(25 * 1024 * 1024);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
    }

    @Test
    public void testInvalidMaxFileSize2() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMaxFileSize(-24 * 1024);

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));
    }

    @Test
    public void testCookie() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .setCookie(new HashMap<>())
            .addCookie("foo", "bar")
            .addCookie("test", "123")
            .addCookie("abc", "cba");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("bar", ((Map<String, String>) config.get("cookie")).get("foo"));
        assertEquals("123", ((Map<String, String>) config.get("cookie")).get("test"));
        assertEquals("cba", ((Map<String, String>) config.get("cookie")).get("abc"));
    }

    @Test
    public void testConfigWithContainsInclude() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setContainsInclude(new ArrayList<>())
            .addContainsInclude("foo.bar.com")
            .addContainsInclude("www.mappIntelligence.com")
            .addContainsInclude("sub.domain.tld");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("foo.bar.com", ((List<String>) config.get("containsInclude")).get(0));
        assertEquals("www.mappIntelligence.com", ((List<String>) config.get("containsInclude")).get(1));
        assertEquals("sub.domain.tld", ((List<String>) config.get("containsInclude")).get(2));
    }

    @Test
    public void testConfigWithContainsExclude() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setContainsExclude(new ArrayList<>())
            .addContainsExclude("foo.bar.com")
            .addContainsExclude("www.mappIntelligence.com")
            .addContainsExclude("sub.domain.tld");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("foo.bar.com", ((List<String>) config.get("containsExclude")).get(0));
        assertEquals("www.mappIntelligence.com", ((List<String>) config.get("containsExclude")).get(1));
        assertEquals("sub.domain.tld", ((List<String>) config.get("containsExclude")).get(2));
    }

    @Test
    public void testConfigWithMatchesInclude() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMatchesInclude(new ArrayList<>())
            .addMatchesInclude("foo\\.bar\\.com")
            .addMatchesInclude("www\\.mappIntelligence\\.com")
            .addMatchesInclude("sub\\.domain\\.tld");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("foo\\.bar\\.com", ((List<String>) config.get("matchesInclude")).get(0));
        assertEquals("www\\.mappIntelligence\\.com", ((List<String>) config.get("matchesInclude")).get(1));
        assertEquals("sub\\.domain\\.tld", ((List<String>) config.get("matchesInclude")).get(2));
    }

    @Test
    public void testConfigWithMatchesExclude() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setMatchesExclude(new ArrayList<>())
            .addMatchesExclude("foo\\.bar\\.com")
            .addMatchesExclude("www\\.mappIntelligence\\.com")
            .addMatchesExclude("sub\\.domain\\.tld");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals("foo\\.bar\\.com", ((List<String>) config.get("matchesExclude")).get(0));
        assertEquals("www\\.mappIntelligence\\.com", ((List<String>) config.get("matchesExclude")).get(1));
        assertEquals("sub\\.domain\\.tld", ((List<String>) config.get("matchesExclude")).get(2));
    }

    @Test
    public void testRequestWithContainsInclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsInclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain1.tld")
            .addContainsInclude("sub.domain2.tld")
            .addContainsInclude("sub.domain3.tld")
            .addContainsInclude("sub.domain.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsInclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain1.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesInclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesInclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain1\\.tld")
            .addMatchesInclude("sub\\.domain2\\.tld")
            .addMatchesInclude("sub\\.domain3\\.tld")
            .addMatchesInclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesInclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesInclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain.tld")
            .addMatchesInclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesInclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain1.tld")
            .addMatchesInclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesInclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain1.tld")
            .addMatchesInclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsExclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsExclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain1.tld")
            .addContainsExclude("sub.domain2.tld")
            .addContainsExclude("sub.domain3.tld")
            .addContainsExclude("sub.domain.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsExclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain1.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesExclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesExclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesExclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesExclude("sub\\.domain1\\.tld")
            .addMatchesExclude("sub\\.domain2\\.tld")
            .addMatchesExclude("sub\\.domain3\\.tld")
            .addMatchesExclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesExclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesExclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesExclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain.tld")
            .addMatchesExclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesExclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain1.tld")
            .addMatchesExclude("sub\\.domain\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsAndMatchesExclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsExclude("sub.domain1.tld")
            .addMatchesExclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsIncludeAndExclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain.tld")
            .addContainsExclude(".html")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsIncludeAndExclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain.tld")
            .addContainsExclude("sub.domain1.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithContainsIncludeAndExclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addContainsInclude("sub.domain1.tld")
            .addContainsExclude("sub.domain1.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesIncludeAndExclude1() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain\\.tld")
            .addMatchesExclude("\\.html")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesIncludeAndExclude2() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain\\.tld")
            .addMatchesExclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(false, config.get("deactivateByInAndExclude"));
    }

    @Test
    public void testRequestWithMatchesIncludeAndExclude3() {
        MappIntelligenceConfig mappIntelligenceConfig = (new MappIntelligenceConfig())
            .addMatchesInclude("sub\\.domain1\\.tld")
            .addMatchesExclude("sub\\.domain1\\.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        Map<String, Object> config = mappIntelligenceConfig.build();
        assertEquals(true, config.get("deactivateByInAndExclude"));
    }
}
