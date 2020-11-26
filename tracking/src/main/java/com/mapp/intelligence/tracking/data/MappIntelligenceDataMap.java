package com.mapp.intelligence.tracking.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceDataMap {
    /**
     * Tracking data.
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * @param value Mapp Intelligence action data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap action(MappIntelligenceAction value) {
        data.put("action", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence campaign data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap campaign(MappIntelligenceCampaign value) {
        data.put("campaign", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence customer data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap customer(MappIntelligenceCustomer value) {
        data.put("customer", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence order data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap order(MappIntelligenceOrder value) {
        data.put("order", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence page data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap page(MappIntelligencePage value) {
        data.put("page", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence product data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap product(MappIntelligenceProductCollection value) {
        data.put("product", value);
        return this;
    }

    /**
     * @param value Mapp Intelligence session data
     * @return MappIntelligenceDataMap
     */
    public MappIntelligenceDataMap session(MappIntelligenceSession value) {
        data.put("session", value);
        return this;
    }

    /**
     * @return Map(String, Object)
     */
    public Map<String, Object> build() {
        return data;
    }
}
