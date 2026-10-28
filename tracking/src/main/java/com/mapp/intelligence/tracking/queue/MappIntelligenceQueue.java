package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerHttpClient;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.data.MappIntelligenceCampaign;
import com.mapp.intelligence.tracking.util.RequestScheduler;
import com.mapp.intelligence.tracking.util.URLString;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceQueue extends MappIntelligenceEnrichment implements AutoCloseable {
    /**
     * Mapp Intelligence request queue.
     */
    private final RequestScheduler requestScheduler;
    /**
     * ID of the first thread that tracked through this instance, 0 as long as there was none.
     */
    private final AtomicLong trackingThreadId = new AtomicLong(0);
    /**
     * Makes sure the shared-usage warning is logged at most once per instance.
     */
    private final AtomicBoolean sharedUsageWarned = new AtomicBoolean(false);

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceQueue(Map<String, Object> config) {
        super(config);

        String consumerType = (String) config.get("consumerType");
        MappIntelligenceConsumer consumer = (MappIntelligenceConsumer) config.get("consumer");
        // a consumer from the config belongs to the caller and may be shared by several instances,
        // so only a consumer created here is closed together with this instance
        boolean ownsConsumer = (consumer == null);
        if (consumer == null) {
            if (consumerType.equals(MappIntelligenceConsumerType.HTTP_CLIENT)) {
                consumer = new MappIntelligenceConsumerHttpClient(config);
            } else if (consumerType.equals(MappIntelligenceConsumerType.FILE)) {
                consumer = new MappIntelligenceConsumerFile(config);
            }
        }

        this.requestScheduler = new RequestScheduler(config, consumer, ownsConsumer);
    }

    /**
     * Warns once as soon as a second thread tracks through this instance.
     *
     * <p>This instance carries request data (user agent, IP, cookies, ever ID) in its fields, so a
     * shared instance mixes the data of different users. Tracking from a thread other than the one
     * that created it is fine, tracking from more than one thread is not.</p>
     */
    private void detectSharedUsage() {
        long currentThreadId = Thread.currentThread().getId();
        long knownThreadId = this.trackingThreadId.get();

        if (knownThreadId == 0) {
            this.trackingThreadId.compareAndSet(0, currentThreadId);
            return;
        }

        if (knownThreadId != currentThreadId && this.sharedUsageWarned.compareAndSet(false, true)) {
            this.logger.warn(MappIntelligenceMessages.INSTANCE_IS_NOT_THREAD_SAFE);
        }
    }

    /**
     * @param data Query parameter map
     * @param key Query parameter key
     * @param value Query parameter value
     */
    private void addQueryParameterToMap(Map<String, String> data, String key, String value) {
        if (!value.isEmpty()) {
            data.put(key, value);
        }
    }

    /**
     * @param str Tracking request data
     * @param data Query parameter map
     * @param key Query parameter key
     * @param value Query parameter value
     */
    private void addNotExistingQueryParameterToMap(String str, Map<String, String> data, String key, String value) {
        // compared by parameter name: a value that contains the name (e.g. cs1=see%20X-WT-UA) does not count
        String query = str.substring(str.indexOf('?') + 1);
        for (String param : query.split("&")) {
            if (param.split("=", 2)[0].equals(key)) {
                return;
            }
        }

        this.addQueryParameterToMap(data, key, value);
    }

    /**
     * @param queueId Queue ID
     *
     * @return List(String)
     */
    public Deque<String> getQueue(int queueId) {
        return this.requestScheduler.getQueue(queueId);
    }

    /**
     * @param data Tracking request
     *
     * @return Queue ID
     */
    private int addToQueue(String data) {
        return this.requestScheduler.add(data);
    }

    /**
     * @param d Tracking request data
     *
     * @return Queue ID
     */
    public int add(String d) {
        this.detectSharedUsage();

        String data = d;
        if (!data.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.USER_AGENT, this.getUserAgent());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.USER_IP, this.getUserIP());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT, this.getClientHintUserAgent());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_FULL_VERSION_LIST, this.getClientHintUserAgentFullVersionList());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MODEL, this.getClientHintUserAgentModel());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MOBILE, this.getClientHintUserAgentMobile());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM, this.getClientHintUserAgentPlatform());
            this.addNotExistingQueryParameterToMap(data, params, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM_VERSION, this.getClientHintUserAgentPlatformVersion());

            data += (!params.isEmpty() ? "&" + URLString.buildQueryString(params) : "");

            return this.addToQueue(data);
        }

        return -1;
    }

    /**
     * @param d Tracking request data
     *
     * @return Queue ID
     */
    public int add(Map<String, String> d) {
        this.detectSharedUsage();

        // a copy: the request data of the caller must not keep the user data (IP, user agent, everID)
        Map<String, String> data = new HashMap<>(d);

        this.addQueryParameterToMap(data, MappIntelligenceParameter.USER_IP, this.getUserIP());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.EVER_ID, this.getEverId());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.USER_AGENT, this.getUserAgent());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT, this.getClientHintUserAgent());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_FULL_VERSION_LIST, this.getClientHintUserAgentFullVersionList());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MODEL, this.getClientHintUserAgentModel());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_MOBILE, this.getClientHintUserAgentMobile());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM, this.getClientHintUserAgentPlatform());
        this.addQueryParameterToMap(data, MappIntelligenceParameter.CLIENT_HINT_USER_AGENT_PLATFORM_VERSION, this.getClientHintUserAgentPlatformVersion());

        String requestURI = this.getRequestURI();
        if (!requestURI.isEmpty()) {
            this.addQueryParameterToMap(data, MappIntelligenceParameter.PAGE_URL, this.getRequestProtocol() + "://" + requestURI);
        }

        String pageName = data.getOrDefault(MappIntelligenceParameter.PAGE_NAME, this.getDefaultPageName());
        data.remove(MappIntelligenceParameter.PAGE_NAME);

        String request = String.format(
            "wt?p=%s&%s",
            this.getMandatoryQueryParameter(pageName),
            URLString.buildQueryString(data)
        );

        return this.addToQueue(request);
    }

    /**
     *
     */
    public void flush() {
        this.requestScheduler.flush();
    }

    /**
     * From now on, the requests of every batch that a flush discards are collected, from all request queues (used by
     * the cron job, which keeps them in a file). Without this call nothing is collected.
     *
     * @return The collected requests - a thread-safe list that the following flushes fill; the requests of a batch are
     *         in their order
     */
    public List<String> collectDiscardedRequests() {
        return this.requestScheduler.collectDiscardedRequests();
    }

    /**
     * @param callback Callback for complete flushing
     */
    public void setOnFlushComplete(BiConsumer<Boolean, Integer> callback) {
        this.requestScheduler.setOnFlushComplete(callback);
    }

    /**
     * Resolves the campaign ID of the given campaign: media codes are read from the request URL, and
     * an already transmitted campaign is replaced when "once per session" is active.
     *
     * <p>The campaign object itself is not modified.</p>
     *
     * @param campaign Campaign data
     *
     * @return The campaign ID to send, may be empty
     */
    public String resolveCampaignId(MappIntelligenceCampaign campaign) {
        return this.resolveCampaignId(campaign.getId(), campaign.getMediaCode(), campaign.isOncePerSession());
    }

    /**
     * Flushes the remaining requests and releases thread pools and consumer.
     */
    @Override
    public void close() {
        this.requestScheduler.close();
    }

    /**
     * Releases thread pools and consumer, optionally without sending the requests that are still in
     * the queues - for a caller that keeps its own copy of them, like the cron job.
     *
     * @param flushRemaining Whether the requests still in the queues are sent before closing
     */
    public void close(boolean flushRemaining) {
        this.requestScheduler.close(flushRemaining);
    }
}
