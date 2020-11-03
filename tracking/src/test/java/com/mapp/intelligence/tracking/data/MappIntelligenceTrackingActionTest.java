package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingActionTest {
    @Test
    public void testNewActionWithoutName() {
        MappIntelligenceAction action = new MappIntelligenceAction();

        Map<String, Object> data = action.getData();
        assertEquals("", data.get("name"));
    }

    @Test
    public void testNewActionWithName() {
        MappIntelligenceAction action = new MappIntelligenceAction("foo.bar");

        Map<String, Object> data = action.getData();
        assertEquals("foo.bar", data.get("name"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligenceAction action = new MappIntelligenceAction();

        Map<String, Object> data = action.getData();
        assertEquals("", data.get("name"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
        assertEquals(0, ((Map<Integer, String>) data.get("goal")).size());
    }

    @Test
    public void testSetName() {
        MappIntelligenceAction action = new MappIntelligenceAction("foo.bar");
        action.setName("bar.foo");

        Map<String, Object> data = action.getData();
        assertEquals("bar.foo", data.get("name"));
    }

    @Test
    public void testSetGoal() {
        MappIntelligenceAction action = new MappIntelligenceAction();
        action
                .setGoal(2, "foo")
                .setGoal(15, "bar");

        Map<String, Object> data = action.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("goal")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("goal")).get(15));
    }

    @Test
    public void testSetParameter() {
        MappIntelligenceAction action = new MappIntelligenceAction();
        action
                .setParameter(2, "foo")
                .setParameter(15, "bar");

        Map<String, Object> data = action.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceAction action = new MappIntelligenceAction();
        action
                .setName("foo.bar")
                .setParameter(2, "param2")
                .setParameter(15, "param15")
                .setGoal(2, "goal2")
                .setGoal(15, "goal15");

        Map<String, String> data = action.getQueryParameter();
        assertEquals("foo.bar", data.get("ct"));
        assertEquals("param2", data.get("ck2"));
        assertEquals("param15", data.get("ck15"));
        assertEquals("goal2", data.get("cb2"));
        assertEquals("goal15", data.get("cb15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceAction action = new MappIntelligenceAction();

        Map<String, String> data = action.getQueryParameter();
        assertEquals(0, data.size());
    }
}
