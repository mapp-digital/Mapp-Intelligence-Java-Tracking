package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

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
     */
    public void track() {
        URL r = this.requestURL;
        if (!this.deactivate && r != null && r.getQuery() != null && !r.getQuery().isEmpty()) {
            this.queue.add("wt?" + this.requestURL.getQuery());
            this.queue.flush();
        }
    }

    /**
     * @param rURL request url with query parameters
     */
    public void track(String rURL) {
        try {
            this.requestURL = new URL(rURL);
        } catch (MalformedURLException | NullPointerException e) {
            // do nothing
        }

        this.track();
    }
}
