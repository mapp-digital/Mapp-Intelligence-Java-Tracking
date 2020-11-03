package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingPageTest {
    @Test
    public void testNewPageWithoutName() {
        MappIntelligencePage page = new MappIntelligencePage();

        Map<String, Object> data = page.getData();
        assertEquals("", data.get("name"));
    }

    @Test
    public void testNewPageWithName() {
        MappIntelligencePage page = new MappIntelligencePage("foo.bar");

        Map<String, Object> data = page.getData();
        assertEquals("foo.bar", data.get("name"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligencePage page = new MappIntelligencePage();

        Map<String, Object> data = page.getData();
        assertEquals("", data.get("name"));
        assertEquals("", data.get("search"));
        assertEquals(0, data.get("numberSearchResults"));
        assertEquals("", data.get("errorMessages"));
        assertEquals(false, data.get("paywall"));
        assertEquals("", data.get("articleTitle"));
        assertEquals("", data.get("contentTags"));
        assertEquals("", data.get("title"));
        assertEquals("", data.get("type"));
        assertEquals("", data.get("length"));
        assertEquals(0, data.get("daysSincePublication"));
        assertEquals("", data.get("testVariant"));
        assertEquals("", data.get("testExperiment"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
        assertEquals(0, ((Map<Integer, String>) data.get("category")).size());
        assertEquals(0, ((Map<Integer, String>) data.get("goal")).size());
    }

    @Test
    public void testSetTestExperiment() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setTestExperiment("name of an experiment");

        Map<String, Object> data = page.getData();
        assertEquals("name of an experiment", data.get("testExperiment"));
    }

    @Test
    public void testSetContentTags() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setContentTags("name of a content tag");

        Map<String, Object> data = page.getData();
        assertEquals("name of a content tag", data.get("contentTags"));
    }

    @Test
    public void testSetPaywall() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setPaywall(true);

        Map<String, Object> data = page.getData();
        assertEquals(true, data.get("paywall"));
    }

    @Test
    public void testSetLength() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setLength("large");

        Map<String, Object> data = page.getData();
        assertEquals("large", data.get("length"));
    }

    @Test
    public void testSetParameter() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setParameter(2, "foo");
        page.setParameter(15, "bar");

        Map<String, Object> data = page.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testSetDaysSincePublication() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setDaysSincePublication(8);

        Map<String, Object> data = page.getData();
        assertEquals(8, data.get("daysSincePublication"));
    }

    @Test
    public void testSetTestVariant() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setTestVariant("name of a variant");

        Map<String, Object> data = page.getData();
        assertEquals("name of a variant", data.get("testVariant"));
    }

    @Test
    public void testSetSearch() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setSearch("search term");

        Map<String, Object> data = page.getData();
        assertEquals("search term", data.get("search"));
    }

    @Test
    public void testSetArticleTitle() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setArticleTitle("name of an article");

        Map<String, Object> data = page.getData();
        assertEquals("name of an article", data.get("articleTitle"));
    }

    @Test
    public void testSetType() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setType("type of a page");

        Map<String, Object> data = page.getData();
        assertEquals("type of a page", data.get("type"));
    }

    @Test
    public void testSetErrorMessages() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setErrorMessages("error message");

        Map<String, Object> data = page.getData();
        assertEquals("error message", data.get("errorMessages"));
    }

    @Test
    public void testSetName() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setName("name of a page");

        Map<String, Object> data = page.getData();
        assertEquals("name of a page", data.get("name"));
    }

    @Test
    public void testSetTitle() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setTitle("title of a page");

        Map<String, Object> data = page.getData();
        assertEquals("title of a page", data.get("title"));
    }

    @Test
    public void testSetNumberSearchResults() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setNumberSearchResults(15);

        Map<String, Object> data = page.getData();
        assertEquals(15, data.get("numberSearchResults"));
    }

    @Test
    public void testSetCategory() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setCategory(2, "foo");
        page.setCategory(15, "bar");

        Map<String, Object> data = page.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("category")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("category")).get(15));
    }

    @Test
    public void testSetGoal() {
        MappIntelligencePage page = new MappIntelligencePage();
        page.setGoal(2, "foo");
        page.setGoal(15, "bar");

        Map<String, Object> data = page.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("goal")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("goal")).get(15));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligencePage page = new MappIntelligencePage();
        page
                .setTestExperiment("name of an experiment")
                .setContentTags("name of a content tag")
                .setPaywall(true)
                .setLength("large")
                .setDaysSincePublication(8)
                .setTestVariant("name of a variant")
                .setSearch("search term")
                .setArticleTitle("name of an article")
                .setType("type of a page")
                .setErrorMessages("error message")
                .setName("name of a page")
                .setTitle("title of a page")
                .setNumberSearchResults(15)
                .setParameter(2, "parameter 2")
                .setParameter(15, "parameter 15")
                .setCategory(2, "category 2")
                .setCategory(15, "category 15")
                .setGoal(2, "goal 2")
                .setGoal(15, "goal 15");

        Map<String, String> data = page.getQueryParameter();
        assertEquals("name of a page", data.get("pn"));
        assertEquals("search term", data.get("is"));
        assertEquals("15", data.get("cp771"));
        assertEquals("error message", data.get("cp772"));
        assertEquals("true", data.get("cp773"));
        assertEquals("name of an article", data.get("cp774"));
        assertEquals("name of a content tag", data.get("cp775"));
        assertEquals("title of a page", data.get("cp776"));
        assertEquals("type of a page", data.get("cp777"));
        assertEquals("large", data.get("cp778"));
        assertEquals("8", data.get("cp779"));
        assertEquals("name of a variant", data.get("cp781"));
        assertEquals("name of an experiment", data.get("cp782"));
        assertEquals("parameter 2", data.get("cp2"));
        assertEquals("parameter 15", data.get("cp15"));
        assertEquals("category 2", data.get("cg2"));
        assertEquals("category 15", data.get("cg15"));
        assertEquals("goal 2", data.get("cb2"));
        assertEquals("goal 15", data.get("cb15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligencePage page = new MappIntelligencePage();

        Map<String, String> data = page.getQueryParameter();
        assertEquals(0, data.size());
    }
}
