package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerHttpClient;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.util.URLString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceQueue extends MappIntelligenceEnrichment {
    /**
     * The maximum request number per batch.
     */
    private final int maxBatchSize;
    /**
     * The number of resend attempts.
     */
    private final int maxAttempt;
    /**
     * The interval of request resend in milliseconds.
     */
    private final int attemptTimeout;
    /**
     * Mapp Intelligence request queue.
     */
    private List<String> queue = new ArrayList<>();
    /**
     * Mapp Intelligence consumer.
     */
    private MappIntelligenceConsumer consumer;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceQueue(Map<String, Object> config) {
        super(config);

        String consumerType = (String) config.get("consumerType");
        this.maxAttempt = (int) config.get("maxAttempt");
        this.attemptTimeout = (int) config.get("attemptTimeout");
        this.maxBatchSize = (int) config.get("maxBatchSize");
        this.consumer = (MappIntelligenceConsumer) config.get("consumer");

        if (this.consumer == null) {
            if (consumerType.equals(MappIntelligenceConsumerType.HTTP_CLIENT)) {
                this.consumer = new MappIntelligenceConsumerHttpClient(config);
            } else if (consumerType.equals(MappIntelligenceConsumerType.FILE)) {
                this.consumer = new MappIntelligenceConsumerFile(config);
            }
        }
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    private boolean sendBatch(List<String> batchContent) {
        if (this.consumer != null) {
            return this.consumer.sendBatch(batchContent);
        }

        return false;
    }

    /**
     * @return boolean
     */
    private boolean flushQueue() {
        int currentQueueSize = this.queue.size();
        boolean wasRequestSuccessful = true;
        this.logger.log(String.format(MappIntelligenceMessages.SENT_BATCH_REQUESTS, currentQueueSize));

        while (currentQueueSize > 0 && wasRequestSuccessful) {
            int batchSize = Integer.min(this.maxBatchSize, currentQueueSize);
            List<String> batchContent = this.queue.subList(0, batchSize);
            this.queue = this.queue.subList(batchSize, currentQueueSize);
            wasRequestSuccessful = this.sendBatch(batchContent);

            if (!wasRequestSuccessful) {
                this.logger.log(MappIntelligenceMessages.BATCH_REQUEST_FAILED);

                batchContent.addAll(this.queue);
                this.queue = batchContent;
            }

            currentQueueSize = this.queue.size();
            this.logger.log(String.format(
                MappIntelligenceMessages.CURRENT_QUEUE_STATUS,
                batchSize,
                currentQueueSize
            ));
        }

        if (currentQueueSize == 0) {
            this.logger.log(MappIntelligenceMessages.QUEUE_IS_EMPTY);
        }

        return wasRequestSuccessful;
    }

    /**
     * @return List(String)
     */
    public List<String> getQueue() {
        return this.queue;
    }

    /**
     * @param d Tracking request data
     */
    public void add(String d) {
        String data = d;
        if (!data.isEmpty()) {
            Map<String, String> params = new HashMap<>();

            if (!data.contains(MappIntelligenceParameter.USER_AGENT)) {
                String ua = this.getUserAgent();
                if (!ua.isEmpty()) {
                    params.put(MappIntelligenceParameter.USER_AGENT, ua);
                }
            }

            if (!data.contains(MappIntelligenceParameter.USER_IP)) {
                String userIP = this.getUserIP();
                if (!userIP.isEmpty()) {
                    params.put(MappIntelligenceParameter.USER_IP, userIP);
                }
            }

            data += (params.size() > 0 ? "&" + URLString.buildQueryString(params) : "");
            this.queue.add(data);

            int currentQueueSize = this.queue.size();
            this.logger.log(String.format(MappIntelligenceMessages.ADD_THE_FOLLOWING_REQUEST_TO_QUEUE, currentQueueSize, data));

            if (currentQueueSize >= this.maxBatchSize) {
                this.flush();
            }
        }
    }

    /**
     * @param data Tracking request data
     */
    public void add(Map<String, String> data) {
        String eid = this.getEverId();
        if (!eid.isEmpty()) {
            data.put(MappIntelligenceParameter.EVER_ID, eid);
        }

        String ua = this.getUserAgent();
        if (!ua.isEmpty()) {
            data.put(MappIntelligenceParameter.USER_AGENT, ua);
        }

        String userIP = this.getUserIP();
        if (!userIP.isEmpty()) {
            data.put(MappIntelligenceParameter.USER_IP, userIP);
        }

        String requestURI = this.getRequestURI();
        if (!requestURI.isEmpty()) {
            data.put(MappIntelligenceParameter.PAGE_URL, "https://" + requestURI);
        }

        String pageName = data.getOrDefault(MappIntelligenceParameter.PAGE_NAME, this.getDefaultPageName());
        data.remove(MappIntelligenceParameter.PAGE_NAME);

        String request = String.format(
            "wt?p=%s&%s",
            this.getMandatoryQueryParameter(pageName),
            URLString.buildQueryString(data)
        );
        this.queue.add(request);

        int currentQueueSize = this.queue.size();
        this.logger.log(String.format(MappIntelligenceMessages.ADD_THE_FOLLOWING_REQUEST_TO_QUEUE, currentQueueSize, request));

        if (currentQueueSize >= this.maxBatchSize) {
            this.flush();
        }
    }

    /**
     * @return boolean
     */
    public boolean flush() {
        int currentAttempt = 0;
        boolean wasRequestSuccessful = false;
        boolean interrupted = false;

        try {
            while (!wasRequestSuccessful && currentAttempt < this.maxAttempt) {
                wasRequestSuccessful = this.flushQueue();
                currentAttempt++;

                if (!wasRequestSuccessful) {
                    try {
                        Thread.sleep(this.attemptTimeout);
                    } catch (InterruptedException e) {
                        interrupted = true;
                        this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        return wasRequestSuccessful;
    }
}
