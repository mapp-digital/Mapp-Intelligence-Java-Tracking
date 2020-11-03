package com.mapp.intelligence.tracking.data;

import java.util.Map;

import com.mapp.intelligence.tracking.MappIntelligence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingSessionTest {
    @Test
    public void testGetDefault() {
        MappIntelligenceSession session = new MappIntelligenceSession();

        Map<String, Object> data = session.getData();
        assertEquals("", data.get("loginStatus"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
    }

    @Test
    public void testSetParameter() {
        MappIntelligenceSession session = new MappIntelligenceSession();
        session.setParameter(2, "foo");
        session.setParameter(15, "bar");

        Map<String, Object> data = session.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testSetLoginStatus() {
        MappIntelligenceSession session = new MappIntelligenceSession();
        session.setLoginStatus("logged in");

        Map<String, Object> data = session.getData();
        assertEquals("logged in", data.get("loginStatus"));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceSession session = new MappIntelligenceSession();
        session
                .setLoginStatus("logged in")
                .setParameter(2, "param2")
                .setParameter(15, "param15");

        Map<String, String> data = session.getQueryParameter();
        assertEquals("logged in", data.get("cs800"));
        assertEquals("param2", data.get("cs2"));
        assertEquals("param15", data.get("cs15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceSession session = new MappIntelligenceSession();

        Map<String, String> data = session.getQueryParameter();
        assertEquals(0, data.size());
    }
}
