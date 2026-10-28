package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
abstract class AbstractMappIntelligenceConsumer implements MappIntelligenceConsumer, AutoCloseable {
    /**
     * Constant for max payload size.
     */
    private static final int MAX_PAYLOAD_SIZE = 24 * 1024 * 1024;
    /**
     * Constant for max batch size.
     */
    private static final int MAX_BATCH_SIZE = 10 * 1000;
    /**
     * Constant for one hundred as double.
     */
    private static final double DOUBLE_100 = 100d;
    /**
     * Constant for one hundred as double.
     */
    private static final int INTEGER_1024 = 1024;
    /**
     * Constant for the default value of max lines per file.
     */
    protected static final int DEFAULT_MAX_FILE_LINES = 10 * 1000;
    /**
     * Constant for the default value of max file duration (30 min).
     */
    protected static final int DEFAULT_MAX_FILE_DURATION = 30 * 60 * 1000;
    /**
     * Constant for the default value of max file size (24 MB).
     */
    protected static final int DEFAULT_MAX_FILE_SIZE = 24 * 1024 * 1024;
    /**
     * Constant for the default value of max connection timeout.
     */
    protected static final int DEFAULT_CONNECT_TIMEOUT = 30 * 1000;
    /**
     * Constant for the default value of max read timeout.
     */
    protected static final int DEFAULT_READ_TIMEOUT = 5 * 1000;

    /**
     * Mapp Intelligence debug logger.
     */
    protected final MappIntelligenceDebugLogger logger;
    /**
     * Your Mapp Intelligence track ID provided by Mapp.
     */
    private final String trackId;
    /**
     * Your Mapp Intelligence tracking domain.
     */
    private final String trackDomain;
    /**
     * Whether a subclass overrides {@link #sendBatch(List)} but not {@link #send(List)}. Such a
     * subclass was written against the boolean API, and its override has to stay in effect although
     * the scheduler now calls {@code send()}.
     */
    private final boolean legacySendBatchOverride;

    /**
     * @param config Mapp Intelligence configuration
     */
    protected AbstractMappIntelligenceConsumer(Map<String, Object> config) {
        this.legacySendBatchOverride = this.overridesOnlySendBatch();
        this.trackDomain = (String) config.getOrDefault("trackDomain", "");
        this.trackId = (String) config.getOrDefault("trackId", "");

        int logLevel = (int) config.get("logLevel");
        this.logger = new MappIntelligenceDebugLogger((MappIntelligenceLogger) config.get("logger"), logLevel);
    }

    /**
     * @return String
     */
    protected final String getUrl() {
        // every request is sent via SSL, the former option forceSSL is ignored
        String url = "https://" + this.trackDomain;
        url += "/" + this.trackId + "/batch";

        return url;
    }

    /**
     * Builds the payload of a batch.
     *
     * @param batchContent List of tracking requests
     *
     * @return The payload, or {@code null} if the batch exceeds a hard limit and can therefore never
     *         be sent - a caller must discard such a batch instead of requeueing it
     */
    protected final String verifyPayload(List<String> batchContent) {
        int currentBatchSize = batchContent.size();
        if (currentBatchSize > MAX_BATCH_SIZE) {
            this.logger.error(MappIntelligenceMessages.TO_LARGE_BATCH_SIZE, MAX_BATCH_SIZE, currentBatchSize);
            return null;
        }

        String payload = String.join(System.lineSeparator(), batchContent);
        if (payload.length() >= MAX_PAYLOAD_SIZE) {
            float length = payload.length();
            double currentPayloadSize = Math.round(length / INTEGER_1024 / INTEGER_1024 * DOUBLE_100) / DOUBLE_100;
            this.logger.error(MappIntelligenceMessages.TO_LARGE_PAYLOAD_SIZE, currentPayloadSize);
            return null;
        }

        return payload;
    }

    /**
     * A batch that exceeds a hard limit is identical on every retry, so requeueing it blocks the
     * queue for everything behind it. It is dropped instead, with an error entry.
     *
     * @param batchContent List of tracking requests
     */
    protected final void discardUnprocessableBatch(List<String> batchContent) {
        this.logger.error(MappIntelligenceMessages.DISCARD_UNPROCESSABLE_BATCH, batchContent.size());
    }

    /**
     * @return true if the runtime class overrides sendBatch() but not send()
     */
    private boolean overridesOnlySendBatch() {
        return this.declaringClassOf("sendBatch") != AbstractMappIntelligenceConsumer.class
            && this.declaringClassOf("send") == AbstractMappIntelligenceConsumer.class;
    }

    /**
     * Finds the class that really implements a method. {@code getMethod()} cannot be used: javac adds
     * bridge methods for the public methods of this package-private class to its public subclasses,
     * so every method would appear to be declared there.
     *
     * @param methodName Name of a method that takes the batch content
     *
     * @return The implementing class, or null if none was found
     */
    private Class<?> declaringClassOf(String methodName) {
        for (Class<?> c = this.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(methodName, List.class);
                if (!method.isBridge() && !method.isSynthetic()) {
                    return c;
                }
            } catch (NoSuchMethodException e) {
                // not declared on this level, go on with the superclass
            }
        }

        return null;
    }

    /**
     * Sends the batch - the actual work of each consumer.
     *
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    protected abstract BatchResult sendBatchContent(List<String> batchContent);

    /**
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    @Override
    public BatchResult send(List<String> batchContent) {
        if (this.legacySendBatchOverride) {
            // keep an existing sendBatch() override of a subclass in effect
            return this.sendBatch(batchContent) ? BatchResult.SENT : BatchResult.RETRY;
        }

        return this.sendBatchContent(batchContent);
    }

    /**
     * A discarded batch counts as sent here: it must leave the queue, otherwise it blocks everything
     * behind it. Use {@link #send(List)} to tell the two apart.
     *
     * <p>Calls {@link #sendBatchContent(List)} and not {@link #send(List)}, so a subclass override that
     * calls {@code super.sendBatch()} cannot end up in itself again.</p>
     *
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    @Override
    public boolean sendBatch(List<String> batchContent) {
        return this.sendBatchContent(batchContent) != BatchResult.RETRY;
    }
}
