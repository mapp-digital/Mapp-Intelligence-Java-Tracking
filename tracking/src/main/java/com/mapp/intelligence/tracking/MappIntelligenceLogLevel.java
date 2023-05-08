package com.mapp.intelligence.tracking;

import java.lang.reflect.Field;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceLogLevel {
    public static final int NONE = 0;
    public static final int FATAL = 1;
    public static final int ERROR = 2;
    public static final int WARN = 3;
    public static final int INFO = 4;
    public static final int DEBUG = 5;

    /**
     * @param ll Integer log level
     *
     * @return Log level name
     */
    public static String getName(int ll) {
        Field[] declaredFields = MappIntelligenceLogLevel.class.getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                int logLvl = field.getInt(MappIntelligenceLogLevel.class);
                if (logLvl == ll) {
                    return field.getName();
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // do nothing
            }
        }

        return null;
    }

    /**
     * @param ll Log level name
     *
     * @return int Integer log level
     */
    public static int getValue(String ll) {
        String logLvl = ll.toUpperCase();

        Field[] declaredFields = MappIntelligenceLogLevel.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(logLvl)) {
                try {
                    return field.getInt(MappIntelligenceLogLevel.class);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // do nothing
                }
            }
        }

        return -1;
    }

    /**
     * Default constructor.
     */
    private MappIntelligenceLogLevel() {
        // do nothing
    }
}
