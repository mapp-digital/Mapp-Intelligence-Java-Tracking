package com.mapp.intelligence.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MappIntelligenceLogLevelTest {


    @Test
    public void testGetNameError() {
        assertEquals("ERROR", MappIntelligenceLogLevel.getName(2));
    }

    @Test
    public void testGetNameDebug() {
        assertEquals("DEBUG", MappIntelligenceLogLevel.getName(5));
    }

    @Test
    public void testGetNameNotFound() {
        assertNull(MappIntelligenceLogLevel.getName(6));
    }

    @Test
    public void testGetValueError() {
        assertEquals(2, MappIntelligenceLogLevel.getValue("ERROR"));
    }

    @Test
    public void testGetValueDebug() {
        assertEquals(5, MappIntelligenceLogLevel.getValue("debug"));
    }

    @Test
    public void testGetValueNotFound() {
        assertEquals(-1, MappIntelligenceLogLevel.getValue("FAILED"));
    }
}
