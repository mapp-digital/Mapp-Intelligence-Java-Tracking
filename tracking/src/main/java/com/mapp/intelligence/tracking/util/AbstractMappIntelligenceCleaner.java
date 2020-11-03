package com.mapp.intelligence.tracking.util;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public abstract class AbstractMappIntelligenceCleaner implements Runnable {
    /**
     * Default constructor.
     */
    protected AbstractMappIntelligenceCleaner() {
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    /**
     * Sends all requests in the queue.
     */
    @Override
    public void run() {
        this.close();
    }

    /**
     *
     */
    protected abstract void close();
}
