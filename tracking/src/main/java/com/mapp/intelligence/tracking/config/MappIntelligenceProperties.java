package com.mapp.intelligence.tracking.config;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceProperties {
    /**
     * Default constructor.
     */
    private MappIntelligenceProperties() {
        // do nothing
    }

    public static final String TRACK_ID = "tracking.trackId";
    public static final String TRACK_DOMAIN = "tracking.trackDomain";
    public static final String DEACTIVATE = "tracking.deactivate";
    public static final String DOMAIN = "tracking.domain";
    public static final String USE_PARAMS_FOR_DEFAULT_PAGE_NAME = "tracking.useParamsForDefaultPageName";
    public static final String CONSUMER_TYPE = "consumer.consumerType";
    public static final String FILE_PATH = "consumer.filePath";
    public static final String FILE_PREFIX = "consumer.filePrefix";
    public static final String MAX_ATTEMPT = "consumer.maxAttempt";
    public static final String ATTEMPT_TIMEOUT = "consumer.attemptTimeout";
    public static final String MAX_BATCH_SIZE = "consumer.maxBatchSize";
    public static final String MAX_QUEUE_SIZE = "consumer.maxQueueSize";
    public static final String MAX_FILE_LINES = "consumer.maxFileLines";
    public static final String MAX_FILE_DURATION = "consumer.maxFileDuration";
    public static final String MAX_FILE_SIZE = "consumer.maxFileSize";
    public static final String FORCE_SSL = "consumer.forceSSL";

    public static final String TMP_DIR = "java.io.tmpdir";
}
