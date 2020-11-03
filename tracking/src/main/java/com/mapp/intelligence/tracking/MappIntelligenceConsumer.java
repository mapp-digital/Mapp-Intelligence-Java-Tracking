package com.mapp.intelligence.tracking;

import java.util.List;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public interface MappIntelligenceConsumer {
    /**
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    boolean sendBatch(List<String> batchContent);
}
