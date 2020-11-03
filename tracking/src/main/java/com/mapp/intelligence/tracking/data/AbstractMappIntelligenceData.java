package com.mapp.intelligence.tracking.data;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public abstract class AbstractMappIntelligenceData {
    /**
     * Filter query parameter.
     */
    protected boolean filterQueryParameter = true;

    /**
     * @param params Category or parameter map
     * @param key    Category or parameter query parameter (e.g. cg, cp, uc, ...)
     * @return Map<String, String>
     */
    protected final Map<String, String> getParameterList(Map<Integer, String> params, String key) {
        Map<String, String> data = new HashMap<>();
        for (Map.Entry<Integer, String> entry : params.entrySet()) {
            data.put(key + entry.getKey(), entry.getValue());
        }

        return data;
    }

    /**
     * @return Map<String, Object>
     */
    public final Map<String, Object> getData() {
        return this.toMap();
    }

    /**
     * @return Map<String, String>
     */
    @SuppressWarnings("unchecked")
    public final Map<String, String> getQueryParameter() {
        Map<String, String> queryList = this.getQueryList();
        Map<String, Object> data = this.getData();
        Map<String, String> queryParameters = new HashMap<>();

        String property;
        String queryParameter;
        for (Map.Entry<String, String> entry : queryList.entrySet()) {
            property = entry.getKey();
            queryParameter = entry.getValue();
            if (data.containsKey(property)) {
                if (data.get(property) instanceof HashMap) {
                    Map<Integer, String> dataMerge = (Map<Integer, String>) data.get(property);
                    queryParameters.putAll(this.getParameterList(dataMerge, queryParameter));
                } else {
                    queryParameters.put(queryParameter, data.get(property).toString());
                }
            }
        }

        if (this.filterQueryParameter) {
            queryParameters.values().removeIf(""::equals);
            queryParameters.values().removeIf("false"::equals);
            queryParameters.values().removeIf("0"::equals);
            queryParameters.values().removeIf("0.0"::equals);
        }

        return queryParameters;
    }

    /**
     * @return Map<String, String>
     */
    protected abstract Map<String, String> getQueryList();

    /**
     * @return Map<String, Object>
     */
    protected abstract Map<String, Object> toMap();
}
