package com.mapp.intelligence.tracking.data;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingEngageTest {
    @Test
    public void testGetDefault() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> data = engage.getData();

        assertEquals("{}", data.get("attributes"));
        assertEquals("", data.get("eventName"));
        assertEquals(0, data.get("eventId"));
    }

    @Test
    public void testSetEventName() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        engage.setEventName("test123");

        Map<String, Object> data = engage.getData();
        assertEquals("test123", data.get("eventName"));
    }

    @Test
    public void testSetEventId() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        engage.setEventId(123456789);

        Map<String, Object> data = engage.getData();
        assertEquals(123456789, data.get("eventId"));
    }

    @Test
    public void testSetAttributes() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("foo", "bar");
        attrs.put("test", 123);
        attrs.put("null", null);
        attrs.put("nan", Double.NaN);

        engage.setAttributes(attrs);

        Map<String, Object> data = engage.getData();
        assertEquals("{\"test\":\"123\",\"foo\":\"bar\"}", data.get("attributes"));
    }

    @Test
    public void testAddAttributes() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();

        Map<String, Object> initialAttrs = new HashMap<>();
        initialAttrs.put("foo", "bar");
        initialAttrs.put("test", 123);
        initialAttrs.put("null", null);
        initialAttrs.put("nan", Double.NaN);

        engage.setAttributes(initialAttrs);

        Map<String, Object> newAttrs = new HashMap<>();
        newAttrs.put("bar", "foo");
        newAttrs.put("test", 456);
        newAttrs.put("null", null);
        newAttrs.put("nan", Double.NaN);

        engage.addAttributes(newAttrs);

        Map<String, Object> data = engage.getData();
        assertEquals("{\"bar\":\"foo\",\"test\":\"456\",\"foo\":\"bar\"}", data.get("attributes"));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("foo", "bar");
        attrs.put("test", 123);
        attrs.put("null", null);
        attrs.put("nan", Double.NaN);

        engage.setEventName("test123")
            .setEventId(123456789)
            .setAttributes(attrs);

        Map<String, String> queryParams = engage.getQueryParameter();

        assertEquals("{\"test\":\"123\",\"foo\":\"bar\"}", queryParams.get("eaj"));
        assertEquals("test123", queryParams.get("ecwen"));
        assertEquals("123456789", queryParams.get("ecwei"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, String> queryParams = engage.getQueryParameter();

        assertEquals(0, queryParams.size());
    }
}
