package com.mapp.intelligence.tracking.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceParameterMap {
    /**
     * Tracking data.
     */
    private Map<String, String> data = new HashMap<>();

    /**
     * @param key Add key to map
     * @param value Add value to map
     *
     * @return MappIntelligenceParameterMap
     */
    public MappIntelligenceParameterMap add(String key, String value) {
        data.put(key, value);
        return this;
    }

    /**
     * @return Map<String, String>
     */
    public Map<String, String> build() {
        return data;
    }
}
