package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceParameter {
    // special parameter
    public static final String USER_AGENT = "X-WT-UA";
    public static final String USER_IP = "X-WT-IP";

    // user agent client hints
    public static final String CLIENT_HINT_USER_AGENT = "X-WT-SEC-CH-UA";
    public static final String CLIENT_HINT_USER_AGENT_FULL_VERSION_LIST = "X-WT-SEC-CH-UA-FULL-VERSION-LIST";
    public static final String CLIENT_HINT_USER_AGENT_MODEL = "X-WT-SEC-CH-UA-MODEL";
    public static final String CLIENT_HINT_USER_AGENT_MOBILE = "X-WT-SEC-CH-UA-MOBILE";
    public static final String CLIENT_HINT_USER_AGENT_PLATFORM = "X-WT-SEC-CH-UA-PLATFORM";
    public static final String CLIENT_HINT_USER_AGENT_PLATFORM_VERSION = "X-WT-SEC-CH-UA-PLATFORM-VERSION";

    // predefined parameter
    public static final String EVER_ID = "eid";
    public static final String CUSTOM_EVER_ID = "ceid";
    public static final String TEMPORARY_SESSION_ID = "fpv";
    public static final String TEMPORARY_SESSION_ID_TYPE = "fpt";
    public static final String PAGE_URL = "pu";
    public static final String ACTION_NAME = "ct";
    public static final String CAMPAIGN_ID = "mc";
    public static final String CAMPAIGN_ACTION = "mca";
    public static final String CUSTOMER_ID = "cd";
    public static final String ORDER_VALUE = "ov";
    public static final String ORDER_ID = "oi";
    public static final String CURRENCY = "cr";
    public static final String PAGE_NAME = "pn";
    public static final String SEARCH = "is";
    public static final String PRODUCT_ID = "ba";
    public static final String PRODUCT_COST = "co";
    public static final String PRODUCT_QUANTITY = "qn";
    public static final String PRODUCT_STATUS = "st";
    public static final String PIXEL_FEATURES = "pf";

    // predefined custom parameter and category
    // predefined urm category
    public static final String EMAIL = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(700);
    public static final String EMAIL_RID = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(701);
    public static final String EMAIL_OPTIN= MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(702);
    public static final String FIRST_NAME = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(703);
    public static final String LAST_NAME = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(704);
    public static final String TELEPHONE = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(705);
    public static final String GENDER = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(706);
    public static final String BIRTHDAY = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(707);
    public static final String COUNTRY = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(708);
    public static final String CITY = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(709);
    public static final String POSTAL_CODE = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(710);
    public static final String STREET = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(711);
    public static final String STREET_NUMBER = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(712);
    public static final String CUSTOMER_VALIDATION = MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(713);
    // predefined e-commerce parameter
    public static final String COUPON_VALUE = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(563);
    public static final String PAYMENT_METHOD = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(761);
    public static final String SHIPPING_SERVICE = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(762);
    public static final String SHIPPING_SPEED = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(763);
    public static final String SHIPPING_COSTS = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(764);
    public static final String GROSS_MARGIN = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(765);
    public static final String ORDER_STATUS = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(766);
    public static final String PRODUCT_VARIANT = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(767);
    public static final String PRODUCT_SOLD_OUT = MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(760);
    // predefined page parameter
    public static final String NUMBER_SEARCH_RESULTS = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(771);
    public static final String ERROR_MESSAGES = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(772);
    public static final String PAYWALL = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(773);
    public static final String ARTICLE_TITLE = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(774);
    public static final String CONTENT_TAGS = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(775);
    public static final String PAGE_TITLE = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(776);
    public static final String PAGE_TYPE = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(777);
    public static final String PAGE_LENGTH = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(778);
    public static final String DAYS_SINCE_PUBLICATION = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(779);
    public static final String TEST_VARIANT = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(781);
    public static final String TEST_EXPERIMENT = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(782);
    // predefined session parameter
    public static final String LOGIN_STATUS = MappIntelligenceCustomParameter.CUSTOM_SESSION_PARAMETER.with(800);
    public static final String VERSION = MappIntelligenceCustomParameter.CUSTOM_SESSION_PARAMETER.with(801);
    public static final String TRACKING_PLATFORM = MappIntelligenceCustomParameter.CUSTOM_SESSION_PARAMETER.with(802);

    // custom parameter and category
    public static final String CUSTOM_SESSION_PARAMETER = "cs";
    public static final String CUSTOM_PAGE_PARAMETER = "cp";
    public static final String CUSTOM_PRODUCT_PARAMETER = "cb";
    public static final String CUSTOM_ACTION_PARAMETER = "ck";
    public static final String CUSTOM_CAMPAIGN_PARAMETER = "cc";
    public static final String CUSTOM_PAGE_CATEGORY = "cg";
    public static final String CUSTOM_PRODUCT_CATEGORY = "ca";
    public static final String CUSTOM_URM_CATEGORY = "uc";

    // cookie names
    public static final String SMART_PIXEL_COOKIE_NAME = "wtstp_eid";
    public static final String PIXEL_COOKIE_NAME = "wt3_eid";
    public static final String SERVER_COOKIE_NAME_PREFIX = "wteid_";

    /**
     * Default constructor.
     */
    private MappIntelligenceParameter() {
        // do nothing
    }
}
