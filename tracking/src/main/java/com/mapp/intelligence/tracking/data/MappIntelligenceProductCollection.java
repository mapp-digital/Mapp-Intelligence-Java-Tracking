package com.mapp.intelligence.tracking.data;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceProductCollection {
    /**
     * List of products.
     */
    private List<MappIntelligenceProduct> data = new ArrayList<>();

    /**
     * @param value Add value to map
     *
     * @return MappIntelligenceProductCollection
     */
    public MappIntelligenceProductCollection add(MappIntelligenceProduct value) {
        data.add(value);
        return this;
    }

    /**
     * @return List<MappIntelligenceProduct>
     */
    public List<MappIntelligenceProduct> build() {
        return data;
    }
}
