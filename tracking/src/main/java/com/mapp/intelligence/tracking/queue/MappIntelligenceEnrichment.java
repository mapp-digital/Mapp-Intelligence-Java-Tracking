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
     * Campaign ID that replaces a campaign which was already transmitted in this session.
     */
    private static final String CAMPAIGN_IGNORE = "ignore%3Dignore";
    /**
     * Separator between the media code name and its value inside a campaign ID.
     */
    private static final String CAMPAIGN_SEPARATOR = "%3D";
    /**
     * Maximum URL encoded length of the transmitted campaigns cookie, older entries are dropped.
     */
    private static final int MAX_TRANSMITTED_CAMPAIGNS_LENGTH = 4000;

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
     * HTTP header for Sec-CH-UA
     */
    private final String clientHintUserAgent;
    /**
     * HTTP header for Sec-CH-UA-Full-Version-List
     */
    private final String clientHintUserAgentFullVersionList;
    /**
     * HTTP header for Sec-CH-UA-Model
     */
    private final String clientHintUserAgentModel;
    /**
     * HTTP header for Sec-CH-UA-Mobile
     */
    private final String clientHintUserAgentMobile;
    /**
     * HTTP header for Sec-CH-UA-Platform
     */
    private final String clientHintUserAgentPlatform;
    /**
     * HTTP header for Sec-CH-UA-Platform-Version
     */
    private final String clientHintUserAgentPlatformVersion;
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
     *
     * <p>The only field of this class that is not effectively final: {@code getUserIdCookie()} fills
     * it lazily. {@code volatile} so the value is visible to a thread that only reads it via
     * {@code getEverId()}, and the lazy initialization is guarded by {@code everIdLock} - without
     * that, two callers could each generate an ID and the cookie handed back to one of them would
     * not match the "eid" that was actually sent.</p>
     */
    private volatile String everId;
    /**
     * Guards the lazy initialization of {@code everId}.
     */
    private final Object everIdLock = new Object();
    /**
     * Campaign IDs that were already transmitted in this session, oldest first.
     */
    private final List<String> transmittedCampaigns = new ArrayList<>();
    /**
     * Whether the transmitted campaigns were already read from the cookie.
     */
    private boolean transmittedCampaignsLoaded;
    /**
     * Whether a new campaign was transmitted and the cookie therefore has to be written back.
     */
    private boolean transmittedCampaignsUpdated;
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
        this.clientHintUserAgent = (String) config.get("clientHintUserAgent");
        this.clientHintUserAgentFullVersionList = (String) config.get("clientHintUserAgentFullVersionList");
        this.clientHintUserAgentModel = (String) config.get("clientHintUserAgentModel");
        this.clientHintUserAgentMobile = (String) config.get("clientHintUserAgentMobile");
        this.clientHintUserAgentPlatform = (String) config.get("clientHintUserAgentPlatform");
        this.clientHintUserAgentPlatformVersion = (String) config.get("clientHintUserAgentPlatformVersion");
        this.remoteAddress = (String) config.get("remoteAddress");
        this.requestURL = (URL) config.get("requestURL");
        this.useParamsForDefaultPageName = (List<String>) config.get("useParamsForDefaultPageName");
        this.cookie = (Map<String, String>) config.get("cookie");
        this.everId = this.getUserId();

        int logLevel = (int) config.get("logLevel");
        MappIntelligenceLogger l = (MappIntelligenceLogger) config.get("logger");
        this.logger = new MappIntelligenceDebugLogger(l, logLevel);
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

        // index 2 is the host in "https://example.com/path", so three elements are required - the
        // guard used to ask for two and then read index 2, which threw for a referrer without a
        // protocol ("example.com/path")
        if (referrerSplit.length >= 3) {
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
                this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
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
    protected final String getClientHintUserAgent() {
        return this.clientHintUserAgent;
    }

    /**
     * @return String
     */
    protected final String getClientHintUserAgentFullVersionList() {
        return this.clientHintUserAgentFullVersionList;
    }

    /**
     * @return String
     */
    protected final String getClientHintUserAgentModel() {
        return this.clientHintUserAgentModel;
    }

    /**
     * @return String
     */
    protected final String getClientHintUserAgentMobile() {
        return this.clientHintUserAgentMobile;
    }

    /**
     * @return String
     */
    protected final String getClientHintUserAgentPlatform() {
        return this.clientHintUserAgentPlatform;
    }

    /**
     * @return String
     */
    protected final String getClientHintUserAgentPlatformVersion() {
        return this.clientHintUserAgentPlatformVersion;
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

        // from the parts of the URL: a "//" in the path or a URL in a query value must not cut it off
        URL r = this.requestURL;
        String uri = r.getAuthority() + r.getPath();
        if (r.getQuery() != null) {
            uri += "?" + r.getQuery();
        }

        if (r.getRef() != null) {
            uri += "#" + r.getRef();
        }

        return URLString.decode(uri);
    }

    /**
     * @return Protocol of the request URL ("http" or "https"), "https" without request URL
     */
    protected final String getRequestProtocol() {
        return (this.requestURL != null) ? this.requestURL.getProtocol() : "https";
    }

    /**
     * @return Map(String, String)
     */
    protected final Map<String, String> getQueryMap() {
        Map<String, String> map = new HashMap<>();
        if (this.requestURL == null) {
            return map;
        }

        String queryString = this.requestURL.getQuery();
        if (queryString != null && !queryString.isEmpty()) {
            // split first, then decode every part: an encoded "&", "=" or "?" stays in its value
            for (String param : queryString.split("&")) {
                if (param.isEmpty()) {
                    continue;
                }

                // limit 2 keeps a "=" inside the value, and a parameter without "=" has no value
                String[] paramSplit = param.split("=", 2);
                map.put(URLString.decode(paramSplit[0]), (paramSplit.length > 1) ? URLString.decode(paramSplit[1]) : "");
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
     * Reads the campaigns that were already transmitted in this session from the "wtstp_tcc" cookie.
     */
    private void readTransmittedCampaigns() {
        if (this.transmittedCampaignsLoaded) {
            return;
        }
        this.transmittedCampaignsLoaded = true;

        String cookieValue = this.getCookieValue(MappIntelligenceParameter.TRANSMITTED_CAMPAIGNS_COOKIE_NAME);
        for (String transmittedCampaign : cookieValue.split(";")) {
            if (!transmittedCampaign.isEmpty() && !this.transmittedCampaigns.contains(transmittedCampaign)) {
                this.transmittedCampaigns.add(transmittedCampaign);
            }
        }
    }

    /**
     * Builds the campaign ID from the media code parameters of the request URL, the same way the
     * client-side pixel does.
     *
     * @param mediaCodes Names of the URL parameters that carry the campaign ID
     *
     * @return String
     */
    private String readCampaignIdFromRequestURL(List<String> mediaCodes) {
        if (mediaCodes == null || mediaCodes.isEmpty()) {
            return "";
        }

        Map<String, String> queryMap = this.getQueryMap();
        List<String> campaignIds = new ArrayList<>();

        for (String mediaCode : mediaCodes) {
            String mediaCodeValue = queryMap.getOrDefault(mediaCode, "");
            if (mediaCodeValue.isEmpty()) {
                continue;
            }

            campaignIds.add(mediaCode + CAMPAIGN_SEPARATOR + mediaCodeValue);
        }

        return String.join(";", campaignIds);
    }

    /**
     * Drops every campaign that was already transmitted in this session and remembers the rest, so
     * they are not transmitted again either.
     *
     * <p>Works per campaign: with {@code ?mc=A&wt_mc=B} and only A already transmitted, B is still
     * sent. Only when nothing new is left is the whole ID replaced by "ignore%3Dignore".</p>
     *
     * @param campaignId Campaign ID of the current request, several IDs separated by ";"
     *
     * @return The campaign ID to send
     */
    private String filterTransmittedCampaigns(String campaignId) {
        List<String> newCampaignIds = new ArrayList<>();
        for (String singleCampaignId : campaignId.split(";")) {
            if (singleCampaignId.isEmpty()
                || singleCampaignId.equals(CAMPAIGN_IGNORE)
                || this.transmittedCampaigns.contains(singleCampaignId)
                || newCampaignIds.contains(singleCampaignId)) {
                continue;
            }

            newCampaignIds.add(singleCampaignId);
        }

        if (newCampaignIds.isEmpty()) {
            return CAMPAIGN_IGNORE;
        }

        this.transmittedCampaigns.addAll(newCampaignIds);
        this.transmittedCampaignsUpdated = true;

        return String.join(";", newCampaignIds);
    }

    /**
     * Resolves the campaign ID that is sent as "mc".
     *
     * <p>An explicitly set ID always wins. Only when none is set are the media code parameters of
     * the request URL evaluated. With "once per session" a campaign that was already transmitted in
     * this session is left out; if none is left, the ID is replaced by "ignore%3Dignore".</p>
     *
     * @param currentId      Explicitly set campaign ID, may be empty
     * @param mediaCodes     Names of the URL parameters that carry the campaign ID
     * @param oncePerSession Whether a campaign is transmitted only once per session
     *
     * @return String
     */
    protected final String resolveCampaignId(String currentId, List<String> mediaCodes, boolean oncePerSession) {
        if (oncePerSession) {
            this.readTransmittedCampaigns();
        }

        String campaignId = (currentId != null) ? currentId : "";
        if (campaignId.isEmpty() && mediaCodes != null) {
            campaignId = this.readCampaignIdFromRequestURL(mediaCodes);
        }

        if (campaignId.isEmpty()) {
            return "";
        }

        if (oncePerSession) {
            campaignId = this.filterTransmittedCampaigns(campaignId);
        }

        return campaignId;
    }

    /**
     * Returns the cookie that carries the campaigns already transmitted in this session, or null if
     * nothing changed. Add it to the HTTP response, otherwise "once per session" cannot work across
     * requests.
     *
     * <p>Deliberately not HttpOnly: the client-side pixel reads the same cookie. It is a session
     * cookie, which mirrors the client-side behaviour.</p>
     *
     * @return MappIntelligenceCookie
     */
    public final MappIntelligenceCookie getTransmittedCampaignsCookie() {
        if (!this.transmittedCampaignsUpdated) {
            return null;
        }

        List<String> campaigns = new ArrayList<>(this.transmittedCampaigns);
        while (campaigns.size() > 1
            && URLString.encode(String.join(";", campaigns)).length() > MAX_TRANSMITTED_CAMPAIGNS_LENGTH) {
            campaigns.remove(0);
        }

        // the value has to be URL encoded: MappIntelligenceConfig.addCookie() decodes incoming
        // cookie values, so without encoding the "%3D" inside a campaign ID would not survive the
        // round trip and an already transmitted campaign would never be recognised again
        MappIntelligenceCookie cookie = new MappIntelligenceServerCookie(
            MappIntelligenceParameter.TRANSMITTED_CAMPAIGNS_COOKIE_NAME,
            URLString.encode(String.join(";", campaigns))
        );
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        // a Secure cookie is neither stored nor sent on plain HTTP pages, and "once per session"
        // would silently never work there
        cookie.setSecure(this.isSecureRequest());
        cookie.setHttpOnly(false);

        return cookie;
    }

    /**
     * @return false only if the request URL is known to be plain HTTP
     */
    private boolean isSecureRequest() {
        return this.requestURL == null || !"http".equalsIgnoreCase(this.requestURL.getProtocol());
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
        synchronized (this.everIdLock) {
            return this.createUserIdCookie(pixelVersion, context);
        }
    }

    /**
     * @param pixelVersion Version ot the current pixel (v4, v5, smart)
     * @param context      Cookie context (1st, 3rd)
     * @return MappIntelligenceCookie
     */
    private MappIntelligenceCookie createUserIdCookie(String pixelVersion, String context) {
        MappIntelligenceCookie c = null;

        if (this.everId.isEmpty()) {
            this.everId = this.generateUserId();

            if (MappIntelligence.SERVER_SIDE_COOKIE.equals(context)) {
                if (this.isOwnTrackDomain()) {
                    String[] cookieDomain = this.trackDomain.split("\\.", 2);

                    // if it is an own tracking domain use this without sub domain
                    c = this.setUserIdCookie(MappIntelligenceParameter.SERVER_COOKIE_NAME_PREFIX + this.trackId, "", cookieDomain[1]);
                }
            } else {
                if (MappIntelligence.V4.equals(pixelVersion) || MappIntelligence.V5.equals(pixelVersion)) {
                    String cookieValue = this.getCookieValue(MappIntelligenceParameter.PIXEL_COOKIE_NAME);
                    cookieValue += String.format(";%s|%s", this.trackId, this.everId);
                    c = this.setUserIdCookie(cookieValue);
                }

                if (MappIntelligence.SMART.equals(pixelVersion)) {
                    c = this.setUserIdCookie();
                }
            }
        }

        return c;
    }
}
