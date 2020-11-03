package com.mapp.intelligence.tracking.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public final class URLString {
    /**
     * Default constructor.
     */
    private URLString() {
        // do nothing
    }

    /**
     * @param str String to be UTF-8 decode
     *
     * @return String
     */
    public static String decode(String str) {
        String decodedString = str;
        try {
            decodedString = URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException | NullPointerException e) {
            // do nothing
        }

        return ((decodedString != null) ? decodedString : "");
    }

    /**
     * @param str String to be UTF-8 encode
     *
     * @return String
     */
    public static String encode(String str) {
        String encodedString = str;
        try {
            encodedString = URLEncoder.encode(str, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException | NullPointerException e) {
            // do nothing
        }

        return ((encodedString != null) ? encodedString : "");
    }

    /**
     * @param data Tracking request data
     *
     * @return String
     */
    public static String buildQueryString(Map<String, String> data) {
        List<String> queryString = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String encodedKey = encode(entry.getKey());
            if (!encodedKey.isEmpty()) {
                queryString.add(String.format("%s=%s", encode(entry.getKey()), encode(entry.getValue())));
            }
        }

        return String.join("&", queryString);
    }
}
