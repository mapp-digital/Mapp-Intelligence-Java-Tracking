package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerHttpClient;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.util.RequestScheduler;
import com.mapp.intelligence.tracking.util.URLString;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceQueue extends MappIntelligenceEnrichment {
    /**
     * Mapp Intelligence request queue.
     */
    private final RequestScheduler requestScheduler;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceQueue(Map<String, Object> config) {
        super(config);

        String consumerType = (String) config.get("consumerType");
        MappIntelligenceConsumer consumer = (MappIntelligenceConsumer) config.get("consumer");
        if (consumer == null) {
            if (consumerType.equals(MappIntelligenceConsumerType.HTTP_CLIENT)) {
                consumer = new MappIntelligenceConsumerHttpClient(config);
            } else if (consumerType.equals(MappIntelligenceConsumerType.FILE)) {
                consumer = new MappIntelligenceConsumerFile(config);
            }
        }

        this.requestScheduler = new RequestScheduler(config, consumer);
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
        if (!str.contains(key)) {
            this.addQueryParameterToMap(data, key, value);
        }
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
     * @param data Tracking request data
     *
     * @return Queue ID
     */
    public int add(Map<String, String> data) {
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
            this.addQueryParameterToMap(data, MappIntelligenceParameter.PAGE_URL, "https://" + requestURI);
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
     * @param callback Callback for complete flushing
     */
    public void setOnFlushComplete(BiConsumer<Boolean, Integer> callback) {
        this.requestScheduler.setOnFlushComplete(callback);
    }
}
