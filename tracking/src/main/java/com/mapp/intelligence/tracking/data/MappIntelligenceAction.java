package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Any action on the website can be tracked as an event in Webtrekk. Events can be as simple as clicking on internal
 * or external links or more advanced such as tracking scroll or content engagement events.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceAction extends AbstractMappIntelligenceData {
    /**
     * Unique identification of the action.
     */
    private String name = "";
    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     * Observe the syntax guidelines when defining parameters.
     */
    private Map<Integer, String> parameter = new HashMap<>();
    /**
     * Needed when website goals are based on events.
     */
    private Map<Integer, String> goal = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceAction() {

    }

    /**
     * @param n Unique identification of the action
     */
    public MappIntelligenceAction(String n) {
        this.name = n;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("name", MappIntelligenceParameter.ACTION_NAME);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_ACTION_PARAMETER);
        queryList.put("goal", MappIntelligenceParameter.CUSTOM_PRODUCT_PARAMETER);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("parameter", this.parameter);
        data.put("goal", this.goal);

        return data;
    }

    /**
     * @param n Unique identification of the action
     *
     * @return MappIntelligenceAction
     */
    public MappIntelligenceAction setName(String n) {
        this.name = n;

        return this;
    }

    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     * Observe the syntax guidelines when defining parameters.
     *
     * @param id    ID of the parameter
     * @param value Value of the parameter
     *
     * @return MappIntelligenceAction
     */
    public MappIntelligenceAction setParameter(int id, String value) {
        this.parameter.put(id, value);

        return this;
    }

    /**
     * Needed when website goals are based on events.
     *
     * @param id    ID of the parameter
     * @param value Value of the parameter
     *
     * @return MappIntelligenceAction
     */
    public MappIntelligenceAction setGoal(int id, String value) {
        this.goal.put(id, value);

        return this;
    }
}
