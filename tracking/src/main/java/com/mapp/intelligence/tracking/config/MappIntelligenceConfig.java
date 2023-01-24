package com.mapp.intelligence.tracking.config;

import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.util.URLString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
     * Deactivate the tracking functionality.
     */
    private boolean deactivateByInAndExclude;
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
     * If the string is contained in the request URL, the request is measured.
     */
    private List<String> containsInclude = new ArrayList<>();
    /**
     * If the string is contained in the request URL, the request isn't measured.
     */
    private List<String> containsExclude = new ArrayList<>();
    /**
     * If the regular expression matches the request URL, the request is measured.
     */
    private List<String> matchesInclude = new ArrayList<>();
    /**
     * If the regular expression matches the request URL, the request isn't measured.
     */
    private List<String> matchesExclude = new ArrayList<>();

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
            .setForceSSL(prop.getBooleanProperty(MappIntelligenceProperties.FORCE_SSL, true))
            .setContainsInclude(prop.getListProperty(MappIntelligenceProperties.CONTAINS_INCLUDE, this.containsInclude, ";"))
            .setContainsExclude(prop.getListProperty(MappIntelligenceProperties.CONTAINS_EXCLUDE, this.containsExclude, ";"))
            .setMatchesInclude(prop.getListProperty(MappIntelligenceProperties.MATCHES_INCLUDE, this.matchesInclude, ";"))
            .setMatchesExclude(prop.getListProperty(MappIntelligenceProperties.MATCHES_EXCLUDE, this.matchesExclude, ";"));
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
     * @param list List of strings, if is contained in the request URL, the request is/isn't measured
     *
     * @return boolean
     */
    private boolean checkContains(List<String> list) {
        for (String s : list) {
            if (this.requestURL.toString().contains(s)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param list List of regular expressions, if it matches the request URL, the request is/isn't measured
     *
     * @return boolean
     */
    private boolean checkMatches(List<String> list) {
        for (String s : list) {
            try {
                if (Pattern.compile(s).matcher(this.requestURL.toString()).find()) {
                    return true;
                }
            }
            catch (PatternSyntaxException e) {
                this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
            }
        }

        return false;
    }

    /**
     * @return boolean
     */
    private boolean isDeactivateByInAndExclude() {
        if (this.requestURL == null) {
            return false;
        }

        boolean isContainsIncludeEmpty = this.containsInclude.isEmpty();
        boolean isMatchesIncludeEmpty = this.matchesInclude.isEmpty();
        boolean isContainsExcludeEmpty = this.containsExclude.isEmpty();
        boolean isMatchesExcludeEmpty = this.matchesExclude.isEmpty();

        boolean isIncluded = isContainsIncludeEmpty && isMatchesIncludeEmpty;

        if (!isContainsIncludeEmpty) {
            isIncluded = this.checkContains(this.containsInclude);
        }

        if (!isIncluded && !isMatchesIncludeEmpty) {
            isIncluded = this.checkMatches(this.matchesInclude);
        }

        if (isIncluded && !isContainsExcludeEmpty) {
            isIncluded = !this.checkContains(this.containsExclude);
        }

        if (isIncluded && !isMatchesExcludeEmpty) {
            isIncluded = !this.checkMatches(this.matchesExclude);
        }

        return !isIncluded;
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
     * @return int
     */
    private int getStatistics() {
        int statistics = 0;

        if (this.useParamsForDefaultPageName.size() > 0) {
            statistics += 1;
        }

        if (this.forceSSL) {
            statistics += 2;
        }

        if (this.logger != null) {
            statistics += 4;
        }

        if (this.consumerType.equals(MappIntelligenceConsumerType.HTTP_CLIENT)) {
            statistics += 32;
        }

        if (this.consumerType.equals(MappIntelligenceConsumerType.FILE)) {
            statistics += 128;
        }

        if (this.consumerType.equals(MappIntelligenceConsumerType.CUSTOM)) {
            statistics += 256;
        }

        return statistics;
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
     * @return Map(String, String)
     */
    private Map<String, String> getOrDefault(Map<String, String> value, Map<String, String> def) {
        return (value != null) ? value : def;
    }

    /**
     * @param value Origin value
     * @param def Default value
     *
     * @return List(String)
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
     * @param ci Specify the strings that must be contained in the request URL to measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setContainsInclude(List<String> ci) {
        this.containsInclude = this.getOrDefault(ci, this.containsInclude);
        return this;
    }

    /**
     * @param ci Specify the string that must be contained in the request URL to measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addContainsInclude(String ci) {
        if (ci != null) {
            this.containsInclude.add(ci);
        }
        return this;
    }

    /**
     * @param ce Specify the strings that must be contained in the request URL to not measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setContainsExclude(List<String> ce) {
        this.containsExclude = this.getOrDefault(ce, this.containsExclude);
        return this;
    }

    /**
     * @param ce Specify the string that must be contained in the request URL to not measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addContainsExclude(String ce) {
        if (ce != null) {
            this.containsExclude.add(ce);
        }
        return this;
    }

    /**
     * @param mi Specify the regular expressions that must be match the request URL to measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMatchesInclude(List<String> mi) {
        this.matchesInclude = this.getOrDefault(mi, this.matchesInclude);
        return this;
    }

    /**
     * @param mi Specify the regular expression that must be match the request URL to measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addMatchesInclude(String mi) {
        if (mi != null) {
            this.matchesInclude.add(mi);
        }
        return this;
    }

    /**
     * @param me Specify the regular expressions that must be match the request URL to not measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig setMatchesExclude(List<String> me) {
        this.matchesExclude = this.getOrDefault(me, this.matchesExclude);
        return this;
    }

    /**
     * @param me Specify the regular expression that must be match the request URL to not measure the request
     *
     * @return MappIntelligenceConfig
     */
    public MappIntelligenceConfig addMatchesExclude(String me) {
        if (me != null) {
            this.matchesExclude.add(me);
        }
        return this;
    }

    /**
     * @return Map(String, Object)
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

        if (!this.containsInclude.isEmpty() || !this.containsExclude.isEmpty() || !this.matchesInclude.isEmpty() || !this.matchesExclude.isEmpty()) {
            this.deactivateByInAndExclude = this.isDeactivateByInAndExclude();
        }

        int statistics = this.getStatistics();

        Map<String, Object> config = new HashMap<>();
        config.put("trackId", this.trackId);
        config.put("trackDomain", this.trackDomain);
        config.put("domain", this.domain);
        config.put("deactivate", this.deactivate);
        config.put("deactivateByInAndExclude", this.deactivateByInAndExclude);
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
        config.put("containsInclude", this.containsInclude);
        config.put("containsExclude", this.containsExclude);
        config.put("matchesInclude", this.matchesInclude);
        config.put("matchesExclude", this.matchesExclude);
        config.put("statistics", statistics);

        return config;
    }
}
