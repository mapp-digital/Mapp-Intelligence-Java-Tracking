package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Campaign tracking is configured in the Webtrekk tool (Configuration - Marketing Configuration). Without this
 * configuration, no campaign information such as campaign clicks will be collected. Visits to certain pages or
 * the entry of defined links can be tracked as campaign clicks. Most importantly, campaign tracking uses specific
 * parameters - so-called media codes - that are added to the target URLs of the ads.
 * <p>
 * Using a media code improves the accuracy of the data collected with the library. Media codes can also be
 * overwritten or supplemented with additional information using campaign parameters.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceCampaign extends AbstractMappIntelligenceData {
    /**
     * Constant for the campaign action.
     */
    private static final String ACTION = "c";

    /**
     * A campaign ID consists of a media code name and its value, separated by "%3D".
     */
    private String id = "";
    /**
     * If you use media codes as a data source for your campaign tracking, entering their name can raise the accuracy
     * of the measurement. Otherwise, accuracy may be reduced by up to 10% if certain firewalls are used, for example.
     */
    private List<String> mediaCode = Arrays.asList("mc", "wt_mc");
    /**
     * Identifier to send a campaign once per session.
     */
    private boolean oncePerSession;
    /**
     * Campaign parameters always refer to an advertising medium.
     */
    private Map<Integer, String> parameter = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceCampaign() {

    }

    /**
     * @param i A campaign ID consists of a media code name and its value, separated by "%3D"
     */
    public MappIntelligenceCampaign(String i) {
        this.id = i;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("id", MappIntelligenceParameter.CAMPAIGN_ID);
        queryList.put("action", MappIntelligenceParameter.CAMPAIGN_ACTION);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_CAMPAIGN_PARAMETER);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("action", ACTION);
        data.put("mediaCode", this.mediaCode);
        data.put("oncePerSession", this.oncePerSession);
        data.put("parameter", this.parameter);

        return data;
    }

    /**
     * @param i A campaign ID consists of a media code name and its value, separated by "%3D"
     *
     * @return MappIntelligenceCampaign
     */
    public MappIntelligenceCampaign setId(String i) {
        this.id = i;

        return this;
    }

    /**
     * @param mc If you use media codes as a data source for your campaign tracking, entering their name can
     *           raise the accuracy of the measurement. Otherwise, accuracy may be reduced by up to 10% if
     *           certain firewalls are used, for example
     * @return MappIntelligenceCampaign
     */
    public MappIntelligenceCampaign setMediaCode(List<String> mc) {
        this.mediaCode = mc;

        return this;
    }

    /**
     * @param ops A campaign ID is tracked only once per session
     *
     * @return MappIntelligenceCampaign
     */
    public MappIntelligenceCampaign setOncePerSession(boolean ops) {
        this.oncePerSession = ops;

        return this;
    }

    /**
     * Campaign parameters always refer to an advertising medium.
     *
     * @param i    ID of the parameter
     * @param value Value of the parameter
     *
     * @return MappIntelligenceCampaign
     */
    public MappIntelligenceCampaign setParameter(int i, String value) {
        this.parameter.put(i, value);

        return this;
    }
}
