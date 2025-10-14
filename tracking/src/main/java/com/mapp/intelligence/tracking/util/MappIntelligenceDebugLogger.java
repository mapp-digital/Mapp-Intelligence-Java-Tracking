package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public final class MappIntelligenceDebugLogger implements MappIntelligenceLogger {
    private static final String DATE_FORMAT = "dd-MMMM-yyyy hh:mm:ss.SSS";
    private static final String MESSAGE_FORMAT = "%s %s [%s]: ";

    /**
     * Mapp Intelligence logger.
     */
    private MappIntelligenceLogger logger;
    /**
     * Defined the debug log level.
     */
    private final int logLevel;

    /**
     * @param l Mapp Intelligence logger.
     * @param ll Debug log level.
     */
    public MappIntelligenceDebugLogger(MappIntelligenceLogger l, int ll) {
        this.logger = l;
        this.logLevel = ll;
    }

    /**
     * @return Current date (dd-MMMM-yyyy hh:mm:ss.SSS)
     */
    private String getCurrentDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MappIntelligenceDebugLogger.DATE_FORMAT);
        return simpleDateFormat.format(new Date());
    }

    /**
     * @param ll Debug log level
     *
     * @return Message prefix
     */
    private String getMessagePrefix(String ll) {
        return String.format(
            MappIntelligenceDebugLogger.MESSAGE_FORMAT,
            this.getCurrentDate(),
            ll,
            MappIntelligenceMessages.MAPP_INTELLIGENCE
        );
    }

    /**
     * @param msg Debug message
     */
    private void logMessage(String msg) {
        if (this.logger != null) {
            this.logger.log(msg);
        }
    }

    /**
     * @param msg Debug message
     */
    @Override
    public void log(String msg) {
        this.logMessage(msg);
    }

    /**
     * @param msg Debug message
     */
    public void fatal(String msg) {
        if (MappIntelligenceLogLevel.FATAL <= this.logLevel) {
            this.log(this.getMessagePrefix("FATAL") + msg);
        }
    }

    /**
     * @param msg Debug message
     */
    public void error(String msg) {
        if (MappIntelligenceLogLevel.ERROR <= this.logLevel) {
            this.log(this.getMessagePrefix("ERROR") + msg);
        }
    }

    /**
     * @param msg Debug message
     */
    public void warn(String msg) {
        if (MappIntelligenceLogLevel.WARN <= this.logLevel) {
            this.log(this.getMessagePrefix("WARN") + msg);
        }
    }

    /**
     * @param msg Debug message
     */
    public void info(String msg) {
        if (MappIntelligenceLogLevel.INFO <= this.logLevel) {
            this.log(this.getMessagePrefix("INFO") + msg);
        }
    }

    /**
     * @param msg Debug message
     */
    public void debug(String msg) {
        if (MappIntelligenceLogLevel.DEBUG <= this.logLevel) {
            this.log(this.getMessagePrefix("DEBUG") + msg);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    @Override
    public void log(String format, Object... args) {
        this.logMessage(String.format(format, args));
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    public void fatal(String format, Object... args) {
        if (MappIntelligenceLogLevel.FATAL <= this.logLevel) {
            this.log(this.getMessagePrefix("FATAL") + format, args);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    public void error(String format, Object... args) {
        if (MappIntelligenceLogLevel.ERROR <= this.logLevel) {
            this.log(this.getMessagePrefix("ERROR") + format, args);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    public void warn(String format, Object... args) {
        if (MappIntelligenceLogLevel.WARN <= this.logLevel) {
            this.log(this.getMessagePrefix("WARN") + format, args);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    public void info(String format, Object... args) {
        if (MappIntelligenceLogLevel.INFO <= this.logLevel) {
            this.log(this.getMessagePrefix("INFO") + format, args);
        }
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    public void debug(String format, Object... args) {
        if (MappIntelligenceLogLevel.DEBUG <= this.logLevel) {
            this.log(this.getMessagePrefix("DEBUG") + format, args);
        }
    }
}
