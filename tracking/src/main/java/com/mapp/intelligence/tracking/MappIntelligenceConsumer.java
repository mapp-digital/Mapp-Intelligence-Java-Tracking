package com.mapp.intelligence.tracking;

import java.util.List;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public interface MappIntelligenceConsumer {
    /**
     * Outcome of sending a single batch.
     */
    enum BatchResult {
        /**
         * The batch was delivered.
         */
        SENT,
        /**
         * The batch could not be delivered right now, it goes back into the queue and is retried.
         */
        RETRY,
        /**
         * The batch can never be sent, no matter how often it is retried (permanent client error,
         * oversized payload). It leaves the queue so it cannot block everything behind it, but it was
         * not delivered either.
         */
        DISCARDED
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    boolean sendBatch(List<String> batchContent);

    /**
     * Sends a batch and reports what happened to it.
     *
     * <p>The result belongs to exactly this call. That matters because one consumer is shared by all
     * queue threads: a counter on the consumer cannot tell which queue a discarded batch came from.
     * Consumers that never discard can keep implementing {@link #sendBatch(List)} only.</p>
     *
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    default BatchResult send(List<String> batchContent) {
        return this.sendBatch(batchContent) ? BatchResult.SENT : BatchResult.RETRY;
    }
}
