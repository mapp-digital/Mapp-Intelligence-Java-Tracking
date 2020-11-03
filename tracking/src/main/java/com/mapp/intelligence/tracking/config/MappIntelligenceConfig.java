package com.mapp.intelligence.tracking.config;

import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.util.URLString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It is possible to create a configuration object directly in your java code, or create a configuration file and
 * link to the configuration file instead. The advantage of using a file instead of creating an object is being able
 * to update the configuration without updating the java code itself.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceConfig {
    /**
     * Constant for port 80.
     */
    private static final int PORT_80 = 80;
    /**
     * Constant for port 443.
     */
    private static final int PORT_443 = 443;
    /**
     * Constant for the default value of max attempt timeout.
     */
    private static final int DEFAULT_ATTEMPT_TIMEOUT = 100;
    /**
     * Constant for the default value of max batch size.
     */
    private static final int DEFAULT_MAX_BATCH_SIZE = 50;
    /**
     * Constant for the default value of max queue size.
     */
    private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
    /**
     * Constant for the default value of max lines per file.
     */
    private static final int DEFAULT_MAX_FILE_LINES = 10 * 1000;
    /**
     * Constant for the default value of max file duration (30 min).
     */
    private static final int DEFAULT_MAX_FILE_DURATION = 30 * 60 * 1000;
    /**
     * Constant for the default value of max file size (24 MB).
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 24 * 1024 * 1024;
    /**
     * Constant for max attempt.
     */
    private static final int MAX_ATTEMPT = 5;
    /**
     * Constant for max attempt timeout.
     */
    private static final int MAX_ATTEMPT_TIMEOUT = 500;

    /**
     * Your Mapp Intelligence track ID provided by Mapp.
     */
    private String trackId = "";
    /**
     * Your Mapp Intelligence tracking domain.
     */
    private String trackDomain = "";
    /**
     * The domains you do not want to identify as an external referrer (e.g. your subdomains).
     */
    private List<String> domain = new ArrayList<>();
    /**
     * Deactivate the tracking functionality.
     */
    private boolean deactivate;
    /**
     * Activates the debug mode.
     */
    private MappIntelligenceLogger logger;
    /**
     * The consumer to use for data transfer to Intelligence.
     */
    private MappIntelligenceConsumer consumer;
    /**
     * The consumer type to use for data transfer to Intelligence.
     */
    private String consumerType = MappIntelligenceConsumerType.HTTP_CLIENT;
    /**
     * The path to your request logging file.
     */
    private String filePath = "";
    /**
     * The prefix name for your request logging file.
     */
    private String filePrefix = "";
    /**
     * The number of resend attempts.
     */
    private int maxAttempt = 1;
    /**
     * The interval of request resend in milliseconds.
     */
    private int attemptTimeout = DEFAULT_ATTEMPT_TIMEOUT;
    /**
     * The maximum request number per batch.
     */
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    /**
     * The maximum number of requests saved in the queue.
     */
    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
    /**
     * The maximum number of maximal lines per file.
     */
    private int maxFileLines = DEFAULT_MAX_FILE_LINES;
    /**
     * The maximum number of maximal file duration.
     */
    private int maxFileDuration = DEFAULT_MAX_FILE_DURATION;
    /**
     * The maximum number of maximal file size.
     */
    private int maxFileSize = DEFAULT_MAX_FILE_SIZE;
    /**
     * Sends every request via SSL.
     */
    private boolean forceSSL = true;
    /**
     * Specific URL parameter(s) in the default page name.
     */
    private List<String> useParamsForDefaultPageName = new ArrayList<>();
    /**
     * HTTP user agent string.
     */
    private String userAgent = "";
    /**
     * Remote address (ip) from the client.
     */
    private String remoteAddress = "";
    /**
     * HTTP referrer URL.
     */
    private String referrerURL = "";
    /**
     * HTTP request URL.
     */
    private URL requestURL;
    /**
     * Map with cookies.
     */
    private Map<String, String> cookie = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceConfig() {
        // do nothing
    }

    /**
     * @param configFile Path to the configuration file (*.properties or *.xml).
     */
    public MappIntelligenceConfig(String configFile) {
        MappIntelligenceConfigProperties prop = new MappIntelligenceConfigProperties(configFile);

        this.setTrackId(prop.getStringProperty(MappIntelligenceProperties.TRACK_ID, this.trackId))
            .setTrackDomain(prop.getStringProperty(MappIntelligenceProperties.TRACK_DOMAIN, this.trackDomain))
            .setDeactivate(prop.getBooleanProperty(MappIntelligenceProperties.DEACTIVATE, false))
            .setDomain(prop.getListProperty(MappIntelligenceProperties.DOMAIN, this.domain))
            .setUseParamsForDefaultPageName(prop.getListProperty(
                MappIntelligenceProperties.USE_PARAMS_FOR_DEFAULT_PAGE_NAME,
                this.useParamsForDefaultPageName
            ))
            .setConsumerType(prop.getConsumerTypeProperty(MappIntelligenceProperties.CONSUMER_TYPE, this.consumerType))
            .setFilePath(prop.getStringProperty(MappIntelligenceProperties.FILE_PATH, this.filePath))
            .setFilePrefix(prop.getStringProperty(MappIntelligenceProperties.FILE_PREFIX, this.filePrefix))
            .setMaxAttempt(prop.getIntegerProperty(MappIntelligenceProperties.MAX_ATTEMPT, this.maxAttempt))
            .setAttemptTimeout(prop.getIntegerProperty(MappIntelligenceProperties.ATTEMPT_TIMEOUT, this.attemptTimeout))
            .setMaxBatchSize(prop.getIntegerProperty(MappIntelligenceProperties.MAX_BATCH_SIZE, this.maxBatchSize))
            .setMaxQueueSize(prop.getIntegerProperty(MappIntelligenceProperties.MAX_QUEUE_SIZE, this.maxQueueSize))
            .setMaxFileLines(prop.getIntegerProperty(MappIntelligenceProperties.MAX_FILE_LINES, this.maxFileLines))
            .setMaxFileDuration(prop.getIntegerProperty(MappIntelligenceProperties.MAX_FILE_DURATION, this.maxFileDuration))
            .setMaxFileSize(prop.getIntegerProperty(MappIntelligenceProperties.MAX_FILE_SIZE, this.maxFileSize))
            .setForceSSL(prop.getBooleanProperty(MappIntelligenceProperties.FORCE_SSL, true));
    }

    /**
     * @param tId Enter your Mapp Intelligence track ID provided by Mapp
     * @param tDomain Enter your Mapp Intelligence tracking URL
     */
    public MappIntelligenceConfig(String tId, String tDomain) {
        this.trackId = (tId != null) ? tId : this.trackId;
        this.trackDomain = (tDomain != null) ? tDomain : this.trackDomain;
    }

    /**
     * @return String
     */
    private String getOwnDomain() {
        if (this.requestURL == null) {
            return "";
        }

        int serverPort = this.requestURL.getPort();
        if (serverPort == PORT_80 || serverPort == PORT_443 || serverPort == -1) {
            return this.requestURL.getHost();
        }

        return String.format("%s:%s", this.requestURL.getHost(), serverPort);
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return String
     */
    private String getOrDefault(String value, String def) {
        return (value != null) ? value : def;
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return Map<String, String>
     */
    private Map<String, String> getOrDefault(Map<String, String> value, Map<String, String> def) {
        return (value != null) ? value : def;
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return List<String>
     */
    private List<String> getOrDefault(List<String> value, List<String> def) {
        return (value != null) ? value : def;
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return MappIntelligenceLogger
     */
    private MappIntelligenceLogger getOrDefault(MappIntelligenceLogger value, MappIntelligenceLogger def) {
        return (value != null) ? value : def;
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return MappIntelligenceConsumer
     */
    private MappIntelligenceConsumer getOrDefault(MappIntelligenceConsumer value, MappIntelligenceConsumer def) {
        return (value != null) ? value : def;
    }

    /**
     * @param tId Enter your Mapp Intelligence track ID provided by Mapp
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setTrackId(String tId) {
        this.trackId = this.getOrDefault(tId, this.trackId);
        return this;
    }

    /**
     * @param tDomain Enter your Mapp Intelligence tracking URL
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setTrackDomain(String tDomain) {
        this.trackDomain = this.getOrDefault(tDomain, this.trackDomain);
        return this;
    }

    /**
     * @param ua HTTP user agent string
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setUserAgent(String ua) {
        this.userAgent = this.getOrDefault(URLString.decode(ua), this.userAgent);
        return this;
    }

    /**
     * @param ra Remote address (ip) from the client
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setRemoteAddress(String ra) {
        this.remoteAddress = this.getOrDefault(URLString.decode(ra), this.remoteAddress);
        return this;
    }

    /**
     * @param refURL HTTP referrer URL
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setReferrerURL(String refURL) {
        this.referrerURL = this.getOrDefault(refURL, this.referrerURL);
        return this;
    }

    /**
     * @param rURL HTTP request URL
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setRequestURL(String rURL) {
        try {
            this.requestURL = new URL(rURL);
        } catch (MalformedURLException e) {
            // do nothing
        }

        return this;
    }

    /**
     * @param cookies Map with cookies
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setCookie(Map<String, String> cookies) {
        Map<String, String> c = this.getOrDefault(cookies, new HashMap<>());
        for (Map.Entry<String, String> entry : c.entrySet()) {
            this.cookie.put(URLString.decode(entry.getKey()), URLString.decode(entry.getValue()));
        }
        return this;
    }

    /**
     * @param name Name of the cookie
     * @param value Value of the cookie
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addCookie(String name, String value) {
        if (name != null && value != null) {
            this.cookie.put(URLString.decode(name), URLString.decode(value));
        }

        return this;
    }

    /**
     * @param d Specify the domains you do not want to identify as an external referrer (e.g. your subdomains)
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setDomain(List<String> d) {
        this.domain = this.getOrDefault(d, this.domain);
        return this;
    }

    /**
     * @param d Specify the domains you do not want to identify as an external referrer (e.g. your subdomains)
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addDomain(String d) {
        if (d != null) {
            this.domain.add(d);
        }
        return this;
    }

    /**
     * @param l Activates the debug mode. The debug mode sends messages to the custom logger class
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setLogger(MappIntelligenceLogger l) {
        this.logger = this.getOrDefault(l, this.logger);
        return this;
    }

    /**
     * @param d Deactivate the tracking functionality
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setDeactivate(boolean d) {
        this.deactivate = d;
        return this;
    }

    /**
     * @param cType Specify the consumer to use for data transfer to Intelligence
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setConsumerType(String cType) {
        this.consumerType = this.getOrDefault(cType, this.consumerType);
        return this;
    }

    /**
     * @param c Specify the consumer to use for data transfer to Intelligence
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setConsumer(MappIntelligenceConsumer c) {
        this.consumer = this.getOrDefault(c, this.consumer);
        return this;
    }

    /**
     * @param f Enter the path to your request logging file. This is only relevant when using file consumer
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setFilePath(String f) {
        this.filePath = this.getOrDefault(f, this.filePath);
        return this;
    }

    /**
     * @param f Enter the file prefix for your request logging file. This is only relevant when using file consumer
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setFilePrefix(String f) {
        this.filePrefix = this.getOrDefault(f, this.filePrefix);
        return this;
    }

    /**
     * @param mAttempt Specify the number of resend attempts. After the maxAttempts have been reached, the requests
     *                   will be deleted even if the sending failed
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxAttempt(int mAttempt) {
        if (mAttempt >= 1 && mAttempt <= MAX_ATTEMPT) {
            this.maxAttempt = mAttempt;
        }

        return this;
    }

    /**
     * @param aTimeout Specify the interval of request resend in milliseconds
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setAttemptTimeout(int aTimeout) {
        if (aTimeout >= 1 && aTimeout <= MAX_ATTEMPT_TIMEOUT) {
            this.attemptTimeout = aTimeout;
        }

        return this;
    }

    /**
     * @param mBatchSize Specify the maximum request number per batch
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxBatchSize(int mBatchSize) {
        this.maxBatchSize = mBatchSize;
        return this;
    }

    /**
     * @param mQueueSize Specify the maximum number of requests saved in the queue
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxQueueSize(int mQueueSize) {
        this.maxQueueSize = mQueueSize;
        return this;
    }

    /**
     * @param mFileLines Specify the number of maximal lines per file.
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxFileLines(int mFileLines) {
        if (mFileLines >= 1 && mFileLines <= DEFAULT_MAX_FILE_LINES) {
            this.maxFileLines = mFileLines;
        }

        return this;
    }

    /**
     * @param mFileDuration Specify the number of maximal file duration.
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxFileDuration(int mFileDuration) {
        if (mFileDuration >= 1 && mFileDuration <= DEFAULT_MAX_FILE_DURATION) {
            this.maxFileDuration = mFileDuration;
        }

        return this;
    }

    /**
     * @param mFileSize Specify the number of maximal file size.
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMaxFileSize(int mFileSize) {
        if (mFileSize >= 1 && mFileSize <= DEFAULT_MAX_FILE_SIZE) {
            this.maxFileSize = mFileSize;
        }

        return this;
    }

    /**
     * @param fSSL Sends every request via SSL
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setForceSSL(boolean fSSL) {
        this.forceSSL = fSSL;
        return this;
    }

    /**
     * @param uParamsForDefaultPageName Includes specific URL parameter(s) in the default page name
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setUseParamsForDefaultPageName(List<String> uParamsForDefaultPageName) {
        this.useParamsForDefaultPageName = this.getOrDefault(uParamsForDefaultPageName, this.useParamsForDefaultPageName);
        return this;
    }

    /**
     * @param uParamsForDefaultPageName Includes specific URL parameter(s) in the default page name
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addUseParamsForDefaultPageName(String uParamsForDefaultPageName) {
        if (uParamsForDefaultPageName != null) {
            this.useParamsForDefaultPageName.add(uParamsForDefaultPageName);
        }
        return this;
    }

    /**
     * @return Map<String, Object>
     */
    public Map<String, Object> build() {
        if (this.domain.isEmpty()) {
            this.domain.add(this.getOwnDomain());
        }

        if (this.consumerType.equals(MappIntelligenceConsumerType.FILE)) {
            if (this.filePath.isEmpty()) {
                this.filePath = String.format("%s/", System.getProperty(MappIntelligenceProperties.TMP_DIR));
            }

            if (this.filePrefix.isEmpty()) {
                this.filePrefix = "MappIntelligenceRequests";
            }

            this.maxBatchSize = 1;
        }

        Map<String, Object> config = new HashMap<>();
        config.put("trackId", this.trackId);
        config.put("trackDomain", this.trackDomain);
        config.put("domain", this.domain);
        config.put("deactivate", this.deactivate);
        config.put("logger", this.logger);
        config.put("consumer", this.consumer);
        config.put("consumerType", this.consumerType);
        config.put("filePath", this.filePath);
        config.put("filePrefix", this.filePrefix);
        config.put("maxAttempt", this.maxAttempt);
        config.put("attemptTimeout", this.attemptTimeout);
        config.put("maxBatchSize", this.maxBatchSize);
        config.put("maxQueueSize", this.maxQueueSize);
        config.put("maxFileLines", this.maxFileLines);
        config.put("maxFileDuration", this.maxFileDuration);
        config.put("maxFileSize", this.maxFileSize);
        config.put("forceSSL", this.forceSSL);
        config.put("useParamsForDefaultPageName", this.useParamsForDefaultPageName);
        config.put("userAgent", this.userAgent);
        config.put("remoteAddress", this.remoteAddress);
        config.put("referrerURL", this.referrerURL);
        config.put("requestURL", this.requestURL);
        config.put("cookie", this.cookie);

        return config;
    }
}
