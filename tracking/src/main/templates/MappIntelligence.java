package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public interface MappIntelligence extends AutoCloseable {
    /**
     * Mapp Intelligence version.
     */
    String VERSION = "${project.version}";

    /**
     * Identifier for pixel v4.
     */
    String V4 = "v4";

    /**
     * Identifier for pixel v5.
     */
    String V5 = "v5";

    /**
     * Identifier for smart pixel.
     */
    String SMART = "smart";

    /**
     * Identifier for 1st party cookies.
     */
    String CLIENT_SIDE_COOKIE = "1";

    /**
     * Identifier for 3rd party cookies.
     */
    String SERVER_SIDE_COOKIE = "3";

    /**
     * @param pixelVersion Version ot the current pixel (v4, v5, smart)
     * @param context Cookie context (CLIENT_SIDE_COOKIE = "1", SERVER_SIDE_COOKIE = "3")
     *
     * @return MappIntelligenceCookie
     */
    MappIntelligenceCookie getUserIdCookie(String pixelVersion, String context);

    /**
     * Sends the remaining requests and releases all resources. Declared without a checked exception,
     * so try-with-resources needs no catch block.
     */
    @Override
    void close();
}
