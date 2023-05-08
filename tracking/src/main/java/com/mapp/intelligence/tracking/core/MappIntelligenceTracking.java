package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.MappIntelligenceParameter;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.data.AbstractMappIntelligenceData;
import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceProduct;
import com.mapp.intelligence.tracking.data.MappIntelligenceProductCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public final class MappIntelligenceTracking extends AbstractMappIntelligence {
    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceTracking(MappIntelligenceConfig config) {
        super(config);
    }

    /**
     * @param maxLength maximal length to simulate empty values
     *
     * @return String[]
     */
    private String[] simulateEmptyValues(Integer maxLength) {
        String[] emptyArray = new String[maxLength];
        for (int i = 0; i < maxLength; i++) {
            emptyArray[i] = "";
        }

        return emptyArray;
    }

    /**
     * @param value value to convert
     *
     * @return String
     */
    private String convertToString(String value) {
        if ("true".equals(value)) {
            return "1";
        }

        return "false".equals(value) ? "0" : value;
    }

    /**
     * @param data Object to check
     *
     * @return boolean
     */
    private boolean isProductList(Object data) {
        return data instanceof MappIntelligenceProductCollection;
    }

    /**
     * @param productInformation List of products
     *
     * @return Map(String, String)
     */
    private Map<String, String> mergeProducts(List<Map<String, String>> productInformation) {
        Map<String, String[]> requestInformation = new HashMap<>();
        Map<String, String> returnValue = new HashMap<>();
        int length = productInformation.size();

        for (int i = 0; i < length; i++) {
            Map<String, String> pi = productInformation.get(i);
            for (Map.Entry<String, String> entry : pi.entrySet()) {
                if (!requestInformation.containsKey(entry.getKey())) {
                    requestInformation.put(entry.getKey(), this.simulateEmptyValues(length));
                }

                requestInformation.get(entry.getKey())[i] = this.convertToString(entry.getValue());

                if (i == length - 1) {
                    String[] joinValue = requestInformation.get(entry.getKey());
                    returnValue.put(entry.getKey(), String.join(";", joinValue));
                }
            }
        }

        return returnValue;
    }

    /**
     * @return boolean
     */
    private boolean isTrackable() {
        if (this.deactivate) {
            this.logger.info(MappIntelligenceMessages.TRACKING_IS_DEACTIVATED);
            return false;
        }

        if (this.trackId.isEmpty() || this.trackDomain.isEmpty()) {
            this.logger.error(MappIntelligenceMessages.REQUIRED_TRACK_ID_AND_DOMAIN_FOR_TRACKING);
            return false;
        }

        if (this.deactivateByInAndExclude) {
            this.logger.info(MappIntelligenceMessages.TRACKING_IS_DEACTIVATED_BY_IN_AND_EXCLUDE);
            return false;
        }

        return true;
    }

    /**
     * @param requestData Tracking request data
     *
     * @return boolean
     */
    private boolean addToRequestQueue(Map<String, String> requestData) {
        requestData.put(MappIntelligenceParameter.PIXEL_FEATURES, this.statistics.toString());
        requestData.put(MappIntelligenceParameter.VERSION, MappIntelligence.VERSION);
        requestData.put(MappIntelligenceParameter.TRACKING_PLATFORM, TRACKING_PLATFORM);

        this.queue.add(requestData);
        return true;
    }

    /**
     * @param data Tracking request data
     *
     * @return Map<String, String>
     */
    private Map<String, String> getRequestData(Map<String, Object> data) {
        Map<String, String> requestData = new HashMap<>();
        Object value;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            value = entry.getValue();

            if (this.isProductList(value)) {
                List<Map<String, String>> products = new ArrayList<>();
                List<MappIntelligenceProduct> valueProductList = ((MappIntelligenceProductCollection) value).build();
                for (MappIntelligenceProduct mappIntelligenceProduct : valueProductList) {
                    products.add(mappIntelligenceProduct.getQueryParameter());
                }
                requestData.putAll(this.mergeProducts(products));
            } else {
                if (value != null) {
                    requestData.putAll(((AbstractMappIntelligenceData) value).getQueryParameter());
                }
            }
        }

        return requestData;
    }

    /**
     * @return boolean
     */
    public boolean track() {
        return this.track(new MappIntelligenceParameterMap());
    }

    /**
     * @param data Mapp Intelligence parameter map
     *
     * @return boolean
     */
    public boolean track(MappIntelligenceParameterMap data) {
        if (this.isTrackable() && data != null) {
            return this.addToRequestQueue(data.build());
        }

        return false;
    }

    /**
     * @param data Mapp Intelligence data map
     *
     * @return boolean
     */
    public boolean track(MappIntelligenceDataMap data) {
        if (this.isTrackable() && data != null) {
            return this.addToRequestQueue(this.getRequestData(data.build()));
        }

        return false;
    }

    /**
     * @return boolean
     */
    public boolean flush() {
        return this.queue.flush();
    }
}
