package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceCookie;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;
import com.mapp.intelligence.tracking.util.AbstractMappIntelligenceCleaner;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
abstract class AbstractMappIntelligence extends AbstractMappIntelligenceCleaner implements MappIntelligence {
    /**
     * Constant for tracking platform.
     */
    protected static final String TRACKING_PLATFORM = "Java";

    /**
     * Deactivate the tracking functionality.
     */
    protected final boolean deactivate;
    /**
     * Deactivate the tracking functionality.
     */
    protected final boolean deactivateByInAndExclude;
    /**
     * Your Mapp Intelligence track ID provided by Mapp.
     */
    protected final String trackId;
    /**
     * Your Mapp Intelligence tracking domain.
     */
    protected final String trackDomain;
    /**
     * Identifier for tracking feature usage
     */
    protected final Integer statistics;
    /**
     * Mapp Intelligence request queue.
     */
    protected final MappIntelligenceQueue queue;
    /**
     * Mapp Intelligence debug logger.
     */
    protected final MappIntelligenceDebugLogger logger;
    /**
     * Enable if you need to differentiate between additional usage options for the tracked data, e.g.,
     * only to your Intelligence instance or to both Intelligence and Engage.
     */
    protected final boolean activateAdvancedPermission;
    /**
     * If activateAdvancedPermission is activated
     * <ul>
     *     <li>0 (default): only Intelligence</li>
     *     <li>3: Intelligence and Engage</li>
     * </ul>
     */
    protected final int advancedPermissionCategory;

    /**
     * @param config Mapp Intelligence configuration
     */
    protected AbstractMappIntelligence(MappIntelligenceConfig config) {
        Map<String, Object> mappIntelligenceConfig = config.build();
        this.queue = new MappIntelligenceQueue(mappIntelligenceConfig);

        int logLevel = (int) mappIntelligenceConfig.get("logLevel");
        MappIntelligenceLogger l = (MappIntelligenceLogger) mappIntelligenceConfig.get("logger");
        this.logger = new MappIntelligenceDebugLogger(l, logLevel);

        this.statistics = (Integer) mappIntelligenceConfig.get("statistics");
        this.deactivate = (boolean) mappIntelligenceConfig.get("deactivate");
        this.deactivateByInAndExclude = (boolean) mappIntelligenceConfig.get("deactivateByInAndExclude");
        this.trackId = (String) mappIntelligenceConfig.get("trackId");
        this.trackDomain = (String) mappIntelligenceConfig.get("trackDomain");

        this.activateAdvancedPermission = (boolean) mappIntelligenceConfig.get("activateAdvancedPermission");
        this.advancedPermissionCategory = (int) mappIntelligenceConfig.get("advancedPermissionCategory");
    }

    /**
     * Sends all requests in the queue.
     */
    @Override
    protected void close() {
        this.flush();
    }

    /**
     *
     */
    public final void flush() {
        this.queue.flush();
    }

    /**
     * @param callback Callback for complete flushing
     */
    public final void setOnFlushComplete(BiConsumer<Boolean, Integer> callback) {
        this.queue.setOnFlushComplete(callback);
    }

    /**
     * @param pixelVersion Version ot the current pixel (v4, v5, smart)
     * @param context Cookie context (1st, 3rd)
     *
     * @return MappIntelligenceCookie
     */
    @Override
    public final MappIntelligenceCookie getUserIdCookie(String pixelVersion, String context) {
        if (this.trackId.isEmpty() || this.trackDomain.isEmpty()) {
            this.logger.error(MappIntelligenceMessages.REQUIRED_TRACK_ID_AND_DOMAIN_FOR_COOKIE);
            return null;
        }

        return this.queue.getUserIdCookie(pixelVersion, context);
    }
}
