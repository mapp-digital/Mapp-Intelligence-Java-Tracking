package com.mapp.intelligence.tracking;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceFormatter extends Formatter {
    /**
     * Default constructor.
     */
    public MappIntelligenceFormatter() {
        super();
    }

    /**
     * @param logRecord Log record
     * @return String
     */
    @Override
    public String format(LogRecord logRecord) {
        return String.format("%s%s", formatMessage(logRecord), System.lineSeparator());
    }
}
