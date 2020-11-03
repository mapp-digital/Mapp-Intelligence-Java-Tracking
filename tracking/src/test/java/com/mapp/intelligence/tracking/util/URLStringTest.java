package com.mapp.intelligence.tracking.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLStringTest {
    @Test
    public void testDecode() {
        assertEquals("", URLString.decode(""));
        assertEquals("", URLString.decode(null));

        assertEquals("üöä", URLString.decode("%C3%BC%C3%B6%C3%A4"));
        assertEquals("Test Test", URLString.decode("Test%20Test"));
        assertEquals("Test Test", URLString.decode("Test+Test"));
    }

    @Test
    public void testEncode() {
        assertEquals("", URLString.encode(""));
        assertEquals("", URLString.encode(null));

        assertEquals("%C3%BC%C3%B6%C3%A4", URLString.encode("üöä"));
        assertEquals("Test%20Test", URLString.encode("Test Test"));
        assertEquals("Test%2BTest", URLString.encode("Test+Test"));
    }

    @Test
    public void testBuildQueryString() {
        Map<String, String> data = new HashMap<>();
        data.put("", "");
        data.put(null, null);
        data.put("test", "töst täst");
        data.put("foo", "bar");

        String queryString = URLString.buildQueryString(data);
        assertEquals("test=t%C3%B6st%20t%C3%A4st&foo=bar", queryString);
    }
}
