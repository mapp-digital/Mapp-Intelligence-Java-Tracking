package com.mapp.intelligence.tracking;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceCronjobLogger implements MappIntelligenceLogger {
    private static final Logger LOGGER = Logger.getLogger(MappIntelligenceCronjobLogger.class.getName());

    /**
     * Default constructor.
     */
    public MappIntelligenceCronjobLogger() {
        Handler handlerObj = new ConsoleHandler();
        handlerObj.setLevel(Level.ALL);

        handlerObj.setFormatter(new MappIntelligenceFormatter());

        LOGGER.addHandler(handlerObj);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    /**
     * @param msg Debug message
     */
    @Override
    public void log(String msg) {
        LOGGER.info(msg);
    }

    /**
     * @param format String format
     * @param args Arguments
     */
    @Override
    public void log(String format, Object... args) {
        this.log(String.format(format, args));
    }
}
