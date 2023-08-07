package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.util.AbstractMappIntelligenceCleaner;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.util.List;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
abstract class AbstractMappIntelligenceConsumer extends AbstractMappIntelligenceCleaner implements MappIntelligenceConsumer {
    /**
     * Constant for max payload size.
     */
    private static final int MAX_PAYLOAD_SIZE = 24 * 1024 * 1024;
    /**
     * Constant for max batch size.
     */
    private static final int MAX_BATCH_SIZE = 10 * 1000;
    /**
     * Constant for one hundred as double.
     */
    private static final double DOUBLE_100 = 100d;
    /**
     * Constant for one hundred as double.
     */
    private static final int INTEGER_1024 = 1024;
    /**
     * Constant for the default value of max lines per file.
     */
    protected static final int DEFAULT_MAX_FILE_LINES = 10 * 1000;
    /**
     * Constant for the default value of max file duration (30 min).
     */
    protected static final int DEFAULT_MAX_FILE_DURATION = 30 * 60 * 1000;
    /**
     * Constant for the default value of max file size (24 MB).
     */
    protected static final int DEFAULT_MAX_FILE_SIZE = 24 * 1024 * 1024;
    /**
     * Constant for the default value of max connection timeout.
     */
    protected static final int DEFAULT_CONNECT_TIMEOUT = 30 * 1000;
    /**
     * Constant for the default value of max read timeout.
     */
    protected static final int DEFAULT_READ_TIMEOUT = 5 * 1000;

    /**
     * Mapp Intelligence debug logger.
     */
    protected final MappIntelligenceDebugLogger logger;
    /**
     * Your Mapp Intelligence track ID provided by Mapp.
     */
    private final String trackId;
    /**
     * Your Mapp Intelligence tracking domain.
     */
    private final String trackDomain;
    /**
     * Sends every request via SSL.
     */
    private final boolean forceSSL;

    /**
     * @param config Mapp Intelligence configuration
     */
    protected AbstractMappIntelligenceConsumer(Map<String, Object> config) {
        this.forceSSL = (boolean) config.getOrDefault("forceSSL", true);
        this.trackDomain = (String) config.getOrDefault("trackDomain", "");
        this.trackId = (String) config.getOrDefault("trackId", "");

        int logLevel = (int) config.get("logLevel");
        this.logger = new MappIntelligenceDebugLogger((MappIntelligenceLogger) config.get("logger"), logLevel);
    }

    /**
     * @return String
     */
    protected final String getUrl() {
        String url = ((this.forceSSL) ? "https://" : "http://");
        url += this.trackDomain;
        url += "/" + this.trackId + "/batch";

        return url;
    }

    /**
     * @param batchContent List of tracking requests
     * @return String
     */
    protected final String verifyPayload(List<String> batchContent) {
        int currentBatchSize = batchContent.size();
        if (currentBatchSize > MAX_BATCH_SIZE) {
            this.logger.error(MappIntelligenceMessages.TO_LARGE_BATCH_SIZE, MAX_BATCH_SIZE, currentBatchSize);
            return "";
        }

        String payload = String.join(System.getProperty("line.separator"), batchContent);
        if (payload.length() >= MAX_PAYLOAD_SIZE) {
            float length = payload.length();
            double currentPayloadSize = (double) Math.round(length / INTEGER_1024 / INTEGER_1024 * DOUBLE_100) / DOUBLE_100;
            this.logger.error(MappIntelligenceMessages.TO_LARGE_PAYLOAD_SIZE, currentPayloadSize);
            return "";
        }

        return payload;
    }
}
