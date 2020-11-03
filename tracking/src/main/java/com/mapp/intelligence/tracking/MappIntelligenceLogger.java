package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public interface MappIntelligenceLogger {
    /**
     * @param msg Debug message
     */
    void log(String msg);

    /**
     * @param format String format
     * @param args Arguments
     */
    void log(String format, Object... args);
}
