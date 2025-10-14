package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MappIntelligenceTrackingRegistrationTest {
    @Test
    public void testGetDefault() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        Map<String, Object> data = registration.getData();

        assertEquals("", data.get("email"));
        assertEquals("", data.get("groupId"));
        assertEquals("", data.get("mode"));
        assertEquals("", data.get("firstName"));
        assertEquals("", data.get("lastName"));
        assertEquals("", data.get("gender"));
        assertEquals("", data.get("title"));
        assertEquals(false, data.get("optIn"));
    }

    @Test
    public void testSetEmail() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setEmail("foo@bar.com");

        Map<String, Object> data = registration.getData();
        assertEquals("foo@bar.com", data.get("email"));
    }

    @Test
    public void testSetGroupId() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setGroupId("123abc");

        Map<String, Object> data = registration.getData();
        assertEquals("123abc", data.get("groupId"));
    }

    @Test
    public void testSetMode() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setMode("c");

        Map<String, Object> data = registration.getData();
        assertEquals("c", data.get("mode"));
    }

    @Test
    public void testSetFirstName() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setFirstName("foo");

        Map<String, Object> data = registration.getData();
        assertEquals("foo", data.get("firstName"));
    }

    @Test
    public void testSetLastName() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setLastName("bar");

        Map<String, Object> data = registration.getData();
        assertEquals("bar", data.get("lastName"));
    }

    @Test
    public void testSetGender() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setGender("m");

        Map<String, Object> data = registration.getData();
        assertEquals("m", data.get("gender"));
    }

    @Test
    public void testSetTitle() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setTitle("Professor");

        Map<String, Object> data = registration.getData();
        assertEquals("Professor", data.get("title"));
    }

    @Test
    public void testGetQueryParameterWithoutOptIn() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setEmail("foo@bar.com")
            .setGroupId("abc123")
            .setMode("c")
            .setFirstName("foo")
            .setLastName("bar")
            .setGender("m")
            .setTitle("Professor");

        Map<String, String> queryParams = registration.getQueryParameter();
        assertEquals(0, queryParams.size());
    }

    @Test
    public void testGetQueryParameterWithOptIn() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        registration.setEmail("foo@bar.com")
            .setGroupId("abc123")
            .setMode("c")
            .setFirstName("foo")
            .setLastName("bar")
            .setGender("m")
            .setTitle("Professor")
            .setOptIn(true);

        Map<String, String> queryParams = registration.getQueryParameter();

        assertEquals("foo@bar.com", queryParams.get("er1"));
        assertEquals("abc123", queryParams.get("er2"));
        assertEquals("c", queryParams.get("er3"));
        assertEquals("foo", queryParams.get("er4"));
        assertEquals("bar", queryParams.get("er5"));
        assertEquals("m", queryParams.get("er6"));
        assertEquals("Professor", queryParams.get("er7"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceRegistration registration = new MappIntelligenceRegistration();
        Map<String, String> queryParams = registration.getQueryParameter();

        assertEquals(0, queryParams.size());
    }
}
