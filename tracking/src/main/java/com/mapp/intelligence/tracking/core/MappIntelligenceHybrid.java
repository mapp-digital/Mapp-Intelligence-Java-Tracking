package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.util.URLString;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceHybrid extends AbstractMappIntelligence {
    /**
     * A 1x1px transparent gif (base64 encoded).
     */
    private static final String PIXEL = "R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
    /**
     * A 1x1px transparent gif in bytes
     */
    private static final byte[] PIXEL_BYTES = Base64.getDecoder().decode(PIXEL);
    /**
     * Maximum length of an accepted pixel request in bytes (the endpoint is public, it must not fill the disk).
     */
    static final int MAX_REQUEST_LENGTH = 65536;
    /**
     * Parameters that only the server adds (IP address, user agent, client hints). The client must not set them, it
     * could otherwise pretend to be another user.
     */
    private static final List<String> SERVER_PARAMETERS = Arrays.asList(
        MappIntelligenceParameter.USER_IP,
        MappIntelligenceParameter.USER_AGENT,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_FULL_VERSION_LIST,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MODEL,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MOBILE,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM,
        MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM_VERSION
    );

    /**
     * HTTP request URL.
     */
    private URL requestURL;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceHybrid(MappIntelligenceConfig config) {
        super(
            config.build().get("consumerType").equals(MappIntelligenceConsumerType.CUSTOM)
            ? config
            : config.setConsumerType(MappIntelligenceConsumerType.FILE)
        );

        this.requestURL = (URL) config.build().get("requestURL");
    }

    /**
     * @return Returns a 1x1px transparent gif
     */
    public byte[] getResponseAsBytes() {
        return PIXEL_BYTES;
    }

    /**
     * @return Returns a 1x1px transparent gif
     */
    public String getResponseAsString() {
        byte[] decodedBytes = Base64.getDecoder().decode(PIXEL);
        return new String(decodedBytes);
    }

    /**
     * Reads all query parameters from the current request and saves them in the file.
     *
     * @return Queue ID
     */
    public int track() {
        URL r = this.requestURL;
        if (this.deactivate || r == null || r.getQuery() == null || r.getQuery().isEmpty()) {
            return -1;
        }

        String query = removeServerParameters(r.getQuery());
        if (!isValidPixelRequest(query)) {
            String excerpt = (query.length() > 100) ? query.substring(0, 100) : query;
            this.logger.warn(MappIntelligenceMessages.INVALID_PIXEL_REQUEST, MAX_REQUEST_LENGTH, excerpt);
            return -1;
        }

        return this.queue.add("wt?" + query);
    }

    /**
     * The parameters that only the server adds (IP address, user agent, client hints) are removed from the request of
     * the client: otherwise a client could pretend to be another user. The library adds them from the configuration, if
     * they are set there.
     *
     * @param queryString Query string of the pixel request
     *
     * @return The query string without the server parameters
     */
    private static String removeServerParameters(String queryString) {
        List<String> params = new ArrayList<>();
        for (String param : queryString.split("&")) {
            String name = URLString.decode(param.split("=", 2)[0]).toUpperCase(Locale.ROOT);
            if (!SERVER_PARAMETERS.contains(name)) {
                params.add(param);
            }
        }

        return String.join("&", params);
    }

    /**
     * @param queryString Query string of the pixel request
     *
     * @return true if the query string contains the pixel parameter "p" and is not too long
     */
    public static boolean isValidPixelRequest(String queryString) {
        if (queryString == null
            || queryString.getBytes(StandardCharsets.UTF_8).length > MAX_REQUEST_LENGTH) {
            return false;
        }

        for (String param : queryString.split("&")) {
            if (param.startsWith("p=") && param.length() > 2) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param rURL request url with query parameters
     *
     * @return Queue ID
     */
    public int track(String rURL) {
        try {
            this.requestURL = new URL(rURL);
        } catch (MalformedURLException | NullPointerException e) {
            // an invalid URL must not track the previous request again
            this.requestURL = null;
        }

        return this.track();
    }
}
