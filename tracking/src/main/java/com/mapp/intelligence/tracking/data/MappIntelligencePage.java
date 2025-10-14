package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Page requests are used to send page-related data to Mapp Intelligence; such as the page name, parameters,
 * or categories, among others.
 * <p>
 * Below you can find all available methods for page requests.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligencePage extends AbstractMappIntelligenceData {
    /**
     * Allows to overwrite the default page naming.
     */
    private String name = "";
    /**
     * Search terms used in internal search.
     */
    private String search = "";
    /**
     * Number of internal search results.
     */
    private int numberSearchResults;
    /**
     * Allows to track error messages.
     */
    private String errorMessages = "";
    /**
     * Allows to mark an article, if it is behind the Paywall.
     */
    private boolean paywall;
    /**
     * Article heading.
     */
    private String articleTitle = "";
    /**
     * Tags of an article.
     */
    private String contentTags = "";
    /**
     * Title of the page.
     */
    private String title = "";
    /**
     * Type of the page (e.g. "overview").
     */
    private String type = "";
    /**
     * Length of the page (e.g. "large").
     */
    private String length = "";
    /**
     * Days since publication.
     */
    private int daysSincePublication;
    /**
     * Name of the test variant.
     */
    private String testVariant = "";
    /**
     * Name of the test.
     */
    private String testExperiment = "";
    /**
     * You can use parameters to enrich Mapp Intelligence data with your own website-specific information and/or metrics.
     */
    private Map<Integer, String> parameter = new HashMap<>();
    /**
     * Page categories (called "Content Groups" in Mapp) are used to group pages to create website areas.
     */
    private Map<Integer, String> category = new HashMap<>();
    /**
     * When using website goals, all central goals are quickly available for analyzing and filtering.
     */
    private Map<Integer, String> goal = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligencePage() {

    }

    /**
     * @param n Allows to overwrite the default page naming
     */
    public MappIntelligencePage(String n) {
        this.name = n;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("name", MappIntelligenceParameter.PAGE_NAME);
        queryList.put("search", MappIntelligenceParameter.SEARCH);
        queryList.put("numberSearchResults", MappIntelligenceParameter.NUMBER_SEARCH_RESULTS);
        queryList.put("errorMessages", MappIntelligenceParameter.ERROR_MESSAGES);
        queryList.put("paywall", MappIntelligenceParameter.PAYWALL);
        queryList.put("articleTitle", MappIntelligenceParameter.ARTICLE_TITLE);
        queryList.put("contentTags", MappIntelligenceParameter.CONTENT_TAGS);
        queryList.put("title", MappIntelligenceParameter.PAGE_TITLE);
        queryList.put("type", MappIntelligenceParameter.PAGE_TYPE);
        queryList.put("length", MappIntelligenceParameter.PAGE_LENGTH);
        queryList.put("daysSincePublication", MappIntelligenceParameter.DAYS_SINCE_PUBLICATION);
        queryList.put("testVariant", MappIntelligenceParameter.TEST_VARIANT);
        queryList.put("testExperiment", MappIntelligenceParameter.TEST_EXPERIMENT);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_PAGE_PARAMETER);
        queryList.put("category", MappIntelligenceParameter.CUSTOM_PAGE_CATEGORY);
        queryList.put("goal", MappIntelligenceParameter.CUSTOM_PRODUCT_PARAMETER);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("search", this.search);
        data.put("numberSearchResults", this.numberSearchResults);
        data.put("errorMessages", this.errorMessages);
        data.put("paywall", this.paywall);
        data.put("articleTitle", this.articleTitle);
        data.put("contentTags", this.contentTags);
        data.put("title", this.title);
        data.put("type", this.type);
        data.put("length", this.length);
        data.put("daysSincePublication", this.daysSincePublication);
        data.put("testVariant", this.testVariant);
        data.put("testExperiment", this.testExperiment);
        data.put("parameter", this.parameter);
        data.put("category", this.category);
        data.put("goal", this.goal);

        return data;
    }

    /**
     * @param n Allows to overwrite the default page naming
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setName(String n) {
        this.name = n;

        return this;
    }

    /**
     * @param s Search terms used in internal search
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setSearch(String s) {
        this.search = s;

        return this;
    }

    /**
     * @param nSearchResults Number of internal search results
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setNumberSearchResults(int nSearchResults) {
        this.numberSearchResults = nSearchResults;

        return this;
    }

    /**
     * @param eMessages Allows to track error messages
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setErrorMessages(String eMessages) {
        this.errorMessages = eMessages;

        return this;
    }

    /**
     * @param p Allows to mark an article, if it is behind the Paywall
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setPaywall(boolean p) {
        this.paywall = p;

        return this;
    }

    /**
     * @param aTitle Article heading
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setArticleTitle(String aTitle) {
        this.articleTitle = aTitle;

        return this;
    }

    /**
     * @param cTags Tags of an article
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setContentTags(String cTags) {
        this.contentTags = cTags;

        return this;
    }

    /**
     * @param t Title of the page
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setTitle(String t) {
        this.title = t;

        return this;
    }

    /**
     * @param t Type of the page (e.g. "overview")
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setType(String t) {
        this.type = t;

        return this;
    }

    /**
     * @param l Length of the page (e.g. "large")
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setLength(String l) {
        this.length = l;

        return this;
    }

    /**
     * @param dSincePublication Days since publication
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setDaysSincePublication(int dSincePublication) {
        this.daysSincePublication = dSincePublication;

        return this;
    }

    /**
     * @param tExperiment Name of the test
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setTestExperiment(String tExperiment) {
        this.testExperiment = tExperiment;

        return this;
    }

    /**
     * @param tVariant Name of the test variant
     *
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setTestVariant(String tVariant) {
        this.testVariant = tVariant;

        return this;
    }

    /**
     * You can use parameters to enrich Mapp Intelligence data with your own website-specific information and/or metrics.
     *
     * @param id    ID of the parameter
     * @param value Value of the parameter
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setParameter(int id, String value) {
        this.parameter.put(id, value);

        return this;
    }

    /**
     * Page categories (called "Content Groups" in Mapp) are used to group pages to create website areas.
     *
     * @param id    ID of the parameter
     * @param value Value of the parameter
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setCategory(int id, String value) {
        this.category.put(id, value);

        return this;
    }

    /**
     * When using website goals, all central goals are quickly available for analyzing and filtering.
     *
     * @param id    ID of the parameter
     * @param value Value of the parameter
     * @return MappIntelligencePage
     */
    public MappIntelligencePage setGoal(int id, String value) {
        this.goal.put(id, value);

        return this;
    }
}
