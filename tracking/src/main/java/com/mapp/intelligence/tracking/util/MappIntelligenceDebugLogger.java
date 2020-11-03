package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public final class MappIntelligenceDebugLogger implements MappIntelligenceLogger {
    /**
     * Mapp Intelligence logger.
     */
    private MappIntelligenceLogger logger;

    /**
     * @param l Mapp Intelligence logger.
     */
    public MappIntelligenceDebugLogger(MappIntelligenceLogger l) {
        this.logger = l;
    }

    /**
     * @param msg Debug message
     */
    @Override
    public void log(String msg) {
        if (this.logger != null) {
            this.logger.log(MappIntelligenceMessages.MAPP_INTELLIGENCE + msg);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    @Override
    public void log(String format, Object... args) {
        if (this.logger != null) {
            this.logger.log(MappIntelligenceMessages.MAPP_INTELLIGENCE + String.format(format, args));
        }
    }
}
