package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.*;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;
import com.mapp.intelligence.tracking.util.URLString;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.PatternSyntaxException;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceEnrichment {
    /**
     * Constant for the max cookie age (6 month).
     */
    private static final int MAX_COOKIE_AGE = 60 * 60 * 24 * 30 * 6;
    /**
     * Constant for the min random number.
     */
    private static final int MIN_RANDOM = 10000;
    /**
     * Constant for the max random number.
     */
    private static final int MAX_RANDOM = 99999;

    /**
     * Your Mapp Intelligence track ID provided by Mapp.
     */
    private final String trackId;
    /**
     * Your Mapp Intelligence tracking domain.
     */
    private final String trackDomain;
    /**
     * The domains you do not want to identify as an external referrer (e.g. your subdomains).
     */
    private final List<String> domain;
    /**
     * HTTP referrer URL.
     */
    private final String referrerURL;
    /**
     * HTTP user agent string.
     */
    private final String userAgent;
    /**
     * Remote address (ip) from the client.
     */
    private final String remoteAddress;
    /**
     * HTTP request URL.
     */
    private final URL requestURL;
    /**
     * Specific URL parameter(s) in the default page name.
     */
    private final List<String> useParamsForDefaultPageName;
    /**
     * Map with cookies.
     */
    private final Map<String, String> cookie;
    /**
     * Mapp Intelligence unique user id.
     */
    private String everId;
    /**
     * Mapp Intelligence debug logger.
     */
    protected final MappIntelligenceDebugLogger logger;

    /**
     * @param config Current tracking configuration
     */
    @SuppressWarnings("unchecked")
    protected MappIntelligenceEnrichment(Map<String, Object> config) {
        this.trackId = (String) config.get("trackId");
        this.trackDomain = (String) config.get("trackDomain");
        this.domain = (List<String>) config.get("domain");
        this.referrerURL = (String) config.get("referrerURL");
        this.userAgent = (String) config.get("userAgent");
        this.remoteAddress = (String) config.get("remoteAddress");
        this.requestURL = (URL) config.get("requestURL");
        this.useParamsForDefaultPageName = (List<String>) config.get("useParamsForDefaultPageName");
        this.cookie = (Map<String, String>) config.get("cookie");
        this.everId = this.getUserId();

        MappIntelligenceLogger l = (MappIntelligenceLogger) config.get("logger");
        this.logger = new MappIntelligenceDebugLogger(l);
    }

    /**
     * @return long
     */
    private long getTimestamp() {
        return (new Date()).getTime();
    }

    /**
     * @param referrer referrer URL
     * @return String
     */
    private String getReferrerDomain(String referrer) {
        String[] referrerSplit = referrer.split("/");
        if (referrerSplit.length >= 2) {
            return referrerSplit[2].toLowerCase();
        }

        return "";
    }

    /**
     * @param referrer referrer URL
     * @return boolean
     */
    private boolean isOwnDomain(String referrer) {
        if ("0".equals(referrer)) {
            return false;
        }

        String referrerDomain = this.getReferrerDomain(referrer);
        for (String d : this.domain) {
            try {
                if (referrerDomain.equals(d) || referrerDomain.matches(d)) {
                    return true;
                }
            } catch (PatternSyntaxException e) {
                this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
            }
        }

        return false;
    }

    /**
     * @return String
     */
    private String getReferrer() {
        String referrer = ((this.referrerURL.isEmpty()) ? "0" : URLString.decode(this.referrerURL));

        if (this.isOwnDomain(referrer)) {
            referrer = "1";
        }

        return URLString.encode(referrer);
    }

    /**
     * @param cookieName Name of the cookie
     * @return String
     */
    private String getCookieValue(String cookieName) {
        return this.cookie.getOrDefault(cookieName, "");
    }

    /**
     * @return String
     */
    private String getUserId() {
        String eid = "";
        String smartPixelCookie = this.getCookieValue(MappIntelligenceParameter.SMART_PIXEL_COOKIE_NAME);
        String trackServerCookie = this.getCookieValue(MappIntelligenceParameter.SERVER_COOKIE_NAME_PREFIX + this.trackId);
        String oldPixelCookie = this.getCookieValue(MappIntelligenceParameter.PIXEL_COOKIE_NAME);

        if (!smartPixelCookie.isEmpty()) {
            eid = smartPixelCookie;
        } else if (!trackServerCookie.isEmpty()) {
            eid = trackServerCookie;
        } else if (!oldPixelCookie.isEmpty()) {
            String[] everIdValues = oldPixelCookie.split(";");
            for (String everIdValue : everIdValues) {
                if (everIdValue.contains(this.trackId + "|")) {
                    String tmpEverId = everIdValue.replaceAll(this.trackId + "\\|", "");
                    eid = tmpEverId.split("#")[0];
                }
            }
        }

        return eid;
    }

    /**
     * @return String
     */
    private String generateUserId() {
        return "8" + this.getTimestamp() + ThreadLocalRandom.current().nextInt(MIN_RANDOM, MAX_RANDOM);
    }

    /**
     * @param n Name of the cookie
     * @param v Value for the cookie
     * @param d Domain for the cookie
     *
     * @return MappIntelligenceCookie
     */
    private MappIntelligenceCookie setUserIdCookie(String n, String v, String d) {
        String value = v;
        if (value.isEmpty()) {
            value = this.everId;
        }

        MappIntelligenceCookie everIdCookie = new MappIntelligenceServerCookie(n, value);
        if (!d.isEmpty()) {
            everIdCookie.setDomain(d);
        }

        everIdCookie.setMaxAge(MAX_COOKIE_AGE);
        everIdCookie.setPath("/");
        everIdCookie.setSecure(true);
        everIdCookie.setHttpOnly(true);

        return everIdCookie;
    }

    /**
     * @return MappIntelligenceCookie
     */
    private MappIntelligenceCookie setUserIdCookie() {
        return this.setUserIdCookie(MappIntelligenceParameter.SMART_PIXEL_COOKIE_NAME, "", "");
    }

    /**
     * @param value Value for the cookie
     *
     * @return MappIntelligenceCookie
     */
    private MappIntelligenceCookie setUserIdCookie(String value) {
        return this.setUserIdCookie(MappIntelligenceParameter.PIXEL_COOKIE_NAME, value, "");
    }

    /**
     * @return boolean
     */
    private boolean isOwnTrackDomain() {
        /*
         * .webtrekk.net          (Germany)
         * .wt-eu02.net           (Germany)
         * .webtrekk-us.net       (USA)
         * .webtrekk-asia.net     (Singapur)
         * .wt-sa.net             (Brasilien)
         */
        return !this.trackDomain.matches(".+\\.(wt-.*|webtrekk|webtrekk-.*)\\.net$");
    }

    /**
     * @return String
     */
    protected final String getUserAgent() {
        return this.userAgent;
    }

    /**
     * @return String
     */
    protected final String getUserIP() {
        return this.remoteAddress;
    }

    /**
     * @return String
     */
    protected final String getRequestURI() {
        if (this.requestURL == null) {
            return "";
        }

        return URLString.decode(this.requestURL.toString()).split("//")[1];
    }

    /**
     * @return Map(String, String)
     */
    protected final Map<String, String> getQueryMap() {
        Map<String, String> map = new HashMap<>();
        if (this.requestURL == null) {
            return map;
        }

        String queryString = URLString.decode(this.requestURL.getQuery());
        if (!queryString.isEmpty()) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] paramSplit = param.split("=");
                map.put(paramSplit[0], paramSplit[1]);
            }
        }

        return map;
    }

    /**
     * @param pageName Name of the page
     * @return String
     */
    protected final String getMandatoryQueryParameter(String pageName) {
        return String.format("600,%s,,,,,%s,%s,,", URLString.encode(pageName), this.getTimestamp(), this.getReferrer());
    }

    /**
     * @return String
     */
    protected final String getDefaultPageName() {
        String plainUrl = this.getRequestURI().split("\\?")[0];
        List<String> parameterList = new ArrayList<>();
        if (!this.useParamsForDefaultPageName.isEmpty()) {
            for (String parameterKey : this.useParamsForDefaultPageName) {
                Map<String, String> queryMap = this.getQueryMap();
                String parameterValue = queryMap.getOrDefault(parameterKey, "");
                if (!parameterValue.isEmpty()) {
                    parameterList.add(parameterKey + "=" + parameterValue);
                }
            }
        }

        if (!parameterList.isEmpty()) {
            plainUrl += "?" + String.join("&", parameterList);
        }

        if (plainUrl.isEmpty()) {
            plainUrl = "0";
        }

        return plainUrl.toLowerCase();
    }

    /**
     * @return string
     */
    protected final String getEverId() {
        return this.everId;
    }

    /**
     * @param pixelVersion Version ot the current pixel (v4, v5, smart)
     * @param context      Cookie context (1st, 3rd)
     * @return MappIntelligenceCookie
     */
    public final MappIntelligenceCookie getUserIdCookie(String pixelVersion, String context) {
        MappIntelligenceCookie c = null;

        if (this.everId.isEmpty()) {
            this.everId = this.generateUserId();

            if (context.equals(MappIntelligence.SERVER_SIDE_COOKIE)) {
                if (this.isOwnTrackDomain()) {
                    String[] cookieDomain = this.trackDomain.split("\\.", 2);

                    // if it is an own tracking domain use this without sub domain
                    c = this.setUserIdCookie(MappIntelligenceParameter.SERVER_COOKIE_NAME_PREFIX + this.trackId, "", cookieDomain[1]);
                }
            } else {
                if (pixelVersion.equals(MappIntelligence.V4) || pixelVersion.equals(MappIntelligence.V5)) {
                    String cookieValue = this.getCookieValue(MappIntelligenceParameter.PIXEL_COOKIE_NAME);
                    cookieValue += String.format(";%s|%s", this.trackId, this.everId);
                    c = this.setUserIdCookie(cookieValue);
                }

                if (pixelVersion.equals(MappIntelligence.SMART)) {
                    c = this.setUserIdCookie();
                }
            }
        }

        return c;
    }
}
