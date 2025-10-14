package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public enum MappIntelligenceCustomParameter {
    CUSTOM_SESSION_PARAMETER(MappIntelligenceParameter.CUSTOM_SESSION_PARAMETER),
    CUSTOM_PAGE_PARAMETER(MappIntelligenceParameter.CUSTOM_PAGE_PARAMETER),
    CUSTOM_PRODUCT_PARAMETER(MappIntelligenceParameter.CUSTOM_PRODUCT_PARAMETER),
    CUSTOM_ACTION_PARAMETER(MappIntelligenceParameter.CUSTOM_ACTION_PARAMETER),
    CUSTOM_CAMPAIGN_PARAMETER(MappIntelligenceParameter.CUSTOM_CAMPAIGN_PARAMETER),
    CUSTOM_REGISTRATION_PARAMETER(MappIntelligenceParameter.CUSTOM_REGISTRATION_PARAMETER),
    CUSTOM_PAGE_CATEGORY(MappIntelligenceParameter.CUSTOM_PAGE_CATEGORY),
    CUSTOM_PRODUCT_CATEGORY(MappIntelligenceParameter.CUSTOM_PRODUCT_CATEGORY),
    CUSTOM_URM_CATEGORY(MappIntelligenceParameter.CUSTOM_URM_CATEGORY);

    /**
     * Query parameter string.
     */
    private final String queryParameter;

    /**
     * @param qp Name of the query parameter string
     */
    MappIntelligenceCustomParameter(String qp) {
        this.queryParameter = qp;
    }

    /**
     * @param id ID of the custom parameter
     * @return String
     */
    public String with(int id) {
        return String.format("%s%s", this.queryParameter, id);
    }
}
