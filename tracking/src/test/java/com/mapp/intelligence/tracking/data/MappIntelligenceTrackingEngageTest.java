package com.mapp.intelligence.tracking.data;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testAttributeValuesAreJSONEscaped() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("quote", "say \"hi\"");

        engage.setAttributes(attrs);

        assertEquals("{\"quote\":\"say \\\"hi\\\"\"}", engage.getData().get("attributes"));
    }

    @Test
    public void testAttributeKeysAndControlCharactersAreJSONEscaped() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("back\\slash", "line1\nline2\ttabbed");

        engage.setAttributes(attrs);

        assertEquals(
            "{\"back\\\\slash\":\"line1\\nline2\\ttabbed\"}",
            engage.getData().get("attributes")
        );
    }

    @Test
    public void testAttributeInjectionIsNotPossible() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        // without escaping this would smuggle a second, forged field into the JSON
        attrs.put("foo", "bar\",\"injected\":\"yes");

        engage.setAttributes(attrs);

        String json = (String) engage.getData().get("attributes");
        assertFalse(json.contains("\",\"injected\":\"yes\""));
        assertEquals("{\"foo\":\"bar\\\",\\\"injected\\\":\\\"yes\"}", json);
    }

    @Test
    public void testAttributeAcceptsAllUsableTypes() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("bool", Boolean.TRUE);
        attrs.put("long", 42L);
        attrs.put("double", 12.5);
        attrs.put("decimal", new java.math.BigDecimal("19.99"));

        engage.setAttributes(attrs);

        String json = (String) engage.getData().get("attributes");
        assertTrue(json.contains("\"bool\":\"true\""));
        assertTrue(json.contains("\"long\":\"42\""));
        assertTrue(json.contains("\"double\":\"12.5\""));
        assertTrue(json.contains("\"decimal\":\"19.99\""));
    }

    @Test
    public void testAttributeDropsUnrepresentableValues() {
        MappIntelligenceEngage engage = new MappIntelligenceEngage();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("nan", Double.NaN);
        attrs.put("posInf", Double.POSITIVE_INFINITY);
        attrs.put("negInf", Double.NEGATIVE_INFINITY);
        attrs.put("nanFloat", Float.NaN);
        attrs.put("nullValue", null);
        attrs.put("emptyString", "");

        engage.setAttributes(attrs);

        assertEquals("{}", engage.getData().get("attributes"));
    }
}
