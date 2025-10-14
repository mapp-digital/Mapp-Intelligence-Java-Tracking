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

    @Test
    public void testGetQueryParamEmptyURL() {
        String id = URLString.getQueryParam("", "eid");

        assertEquals("", id);
    }

    @Test
    public void testGetQueryParamInvalidURL1() {
        String id = URLString.getQueryParam("localhost/wt?eid=123456", "eid");

        assertEquals("", id);
    }

    @Test
    public void testGetQueryParamInvalidURL2() {
        String id = URLString.getQueryParam(null, "eid");

        assertEquals("", id);
    }

    @Test
    public void testGetQueryParamNotExist() {
        String id = URLString.getQueryParam("https://localhost/wt?id=", "eid");

        assertEquals("", id);
    }

    @Test
    public void testGetQueryParamEmptyParam() {
        String id = URLString.getQueryParam("https://localhost/wt?eid=", "eid");

        assertEquals("", id);
    }

    @Test
    public void testGetQueryParam() {
        String id = URLString.getQueryParam("https://localhost/wt?eid=123456789", "eid");

        assertEquals("123456789", id);
    }
}
