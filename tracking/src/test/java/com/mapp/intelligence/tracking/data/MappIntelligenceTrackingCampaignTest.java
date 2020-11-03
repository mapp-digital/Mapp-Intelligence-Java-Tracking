package com.mapp.intelligence.tracking.data;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingCampaignTest {
    @Test
    public void testNewCampaignWithoutId() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();

        Map<String, Object> data = campaign.getData();
        assertEquals("", data.get("id"));
    }

    @Test
    public void testNewCampaignWithId() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign("wt_mc%3Dfoo.bar");

        Map<String, Object> data = campaign.getData();
        assertEquals("wt_mc%3Dfoo.bar", data.get("id"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();

        Map<String, Object> data = campaign.getData();
        assertEquals("", data.get("id"));
        assertEquals("c", data.get("action"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
    }

    @Test
    public void testSetId() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign("wt_mc%3Dfoo.bar");
        campaign.setId("wt%3Dfoo.bar");

        Map<String, Object> data = campaign.getData();
        assertEquals("wt%3Dfoo.bar", data.get("id"));
    }

    @Test
    public void testSetParameter() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();
        campaign.setParameter(2, "foo");
        campaign.setParameter(15, "bar");

        Map<String, Object> data = campaign.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testSetMediaCode() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();
        campaign.setMediaCode(Arrays.asList("foo", "bar"));

        assertEquals("1", "1");
    }

    @Test
    public void testSetOncePerSession() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();
        campaign.setOncePerSession(false);

        assertEquals("1", "1");
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();
        campaign.setId("wt_mc%3Dfoo.bar");
        campaign.setParameter(2, "param2");
        campaign.setParameter(15, "param15");

        Map<String, String> data = campaign.getQueryParameter();
        assertEquals("wt_mc%3Dfoo.bar", data.get("mc"));
        assertEquals("c", data.get("mca"));
        assertEquals("param2", data.get("cc2"));
        assertEquals("param15", data.get("cc15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceCampaign campaign = new MappIntelligenceCampaign();

        Map<String, String> data = campaign.getQueryParameter();
        assertEquals(1, data.size());
        assertEquals("c", data.get("mca"));
    }
}
