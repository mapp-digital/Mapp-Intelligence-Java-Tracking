package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.MappIntelligenceCookie;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.data.MappIntelligenceCampaign;
import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;

import java.util.Arrays;
import java.util.Deque;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Covers the campaign resolution that mirrors the client-side pixel: media codes are read from the
 * request URL, and "once per session" suppresses a campaign that was already transmitted.
 */
public class MappIntelligenceCampaignResolutionTest {
    /**
     * @param requestURL Request URL of the simulated HTTP request
     * @param cookieName Name of an incoming cookie, may be null
     * @param cookieValue Value of that cookie
     *
     * @return MappIntelligenceTracking
     */
    private MappIntelligenceTracking newTracking(String requestURL, String cookieName, String cookieValue) {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setRequestURL(requestURL);
        mic.setRequestQueues(0);

        if (cookieName != null) {
            mic.addCookie(cookieName, cookieValue);
        }

        return new MappIntelligenceTracking(mic);
    }

    /**
     * @param tracking Tracking instance
     *
     * @return The "mc" parameter of the single queued request
     */
    private String getCampaignParameter(MappIntelligenceTracking tracking) {
        Deque<String> requests = MappIntelligenceUnitUtil.getQueue(tracking, 0);
        assertEquals(1, requests.size());

        for (String parameter : requests.peek().split("&")) {
            if (parameter.startsWith("mc=")) {
                return parameter.substring("mc=".length());
            }
        }

        return "";
    }

    @Test
    public void testMediaCodeIsReadFromRequestURL() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        assertEquals("wt_mc%253Dnewsletter.2026", this.getCampaignParameter(tracking));
        tracking.close();
    }

    @Test
    public void testDefaultMediaCodeMcIsReadFromRequestURL() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?mc=banner.top", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        assertEquals("mc%253Dbanner.top", this.getCampaignParameter(tracking));
        tracking.close();
    }

    @Test
    public void testCustomMediaCodeName() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?my_camp=spring.sale", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setMediaCode(Arrays.asList("my_camp"))
        ));

        assertEquals("my_camp%253Dspring.sale", this.getCampaignParameter(tracking));
        tracking.close();
    }

    @Test
    public void testExplicitCampaignIdWinsOverRequestURL() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(
            new MappIntelligenceCampaign("mc%3Dexplicit")
        ));

        assertEquals("mc%253Dexplicit", this.getCampaignParameter(tracking));
        tracking.close();
    }

    @Test
    public void testNoMediaCodeInRequestURLMeansNoCampaign() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?unrelated=1", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        assertEquals("", this.getCampaignParameter(tracking));
        tracking.close();
    }

    @Test
    public void testOncePerSessionFirstVisitSendsCampaignAndReturnsCookie() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));

        assertEquals("wt_mc%253Dnewsletter.2026", this.getCampaignParameter(tracking));

        MappIntelligenceCookie cookie = tracking.getTransmittedCampaignsCookie();
        assertNotNull(cookie);
        assertEquals("wtstp_tcc", cookie.getName());
        assertEquals("wt_mc%253Dnewsletter.2026", cookie.getValue());
        // session cookie, mirrors the client-side pixel which writes no expiry
        assertEquals(-1, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        // the client-side pixel reads the same cookie, so it must not be HttpOnly
        assertFalse(cookie.isHttpOnly());

        tracking.close();
    }

    @Test
    public void testOncePerSessionSecondVisitSendsIgnore() {
        MappIntelligenceTracking first = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);
        first.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));
        MappIntelligenceCookie cookie = first.getTransmittedCampaignsCookie();
        first.close();

        // the browser sends the cookie back with the next request
        MappIntelligenceTracking second = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", cookie.getName(), cookie.getValue());
        second.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));

        assertEquals("ignore%253Dignore", this.getCampaignParameter(second));
        second.close();
    }

    @Test
    public void testWithoutOncePerSessionTheCampaignIsSentAgain() {
        MappIntelligenceTracking first = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);
        first.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));
        MappIntelligenceCookie cookie = first.getTransmittedCampaignsCookie();
        first.close();

        MappIntelligenceTracking second = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", cookie.getName(), cookie.getValue());
        second.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        assertEquals("wt_mc%253Dnewsletter.2026", this.getCampaignParameter(second));
        second.close();
    }

    @Test
    public void testNoCookieWithoutOncePerSession() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        assertNull(tracking.getTransmittedCampaignsCookie());
        tracking.close();
    }

    @Test
    public void testOncePerSessionWithExplicitCampaignId() {
        MappIntelligenceTracking first = this.newTracking("https://shop.example/page", null, null);
        first.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign("mc%3Dexplicit")).setOncePerSession(true)
        ));
        assertEquals("mc%253Dexplicit", this.getCampaignParameter(first));
        MappIntelligenceCookie cookie = first.getTransmittedCampaignsCookie();
        first.close();

        MappIntelligenceTracking second = this.newTracking(
            "https://shop.example/page", cookie.getName(), cookie.getValue());
        second.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign("mc%3Dexplicit")).setOncePerSession(true)
        ));

        assertEquals("ignore%253Dignore", this.getCampaignParameter(second));
        second.close();
    }

    @Test
    public void testSeveralMediaCodesInOneRequestURL() {
        MappIntelligenceTracking tracking = this.newTracking(
            "https://shop.example/page?mc=banner.top&wt_mc=newsletter.2026", null, null);

        tracking.track((new MappIntelligenceDataMap()).campaign(new MappIntelligenceCampaign()));

        String campaign = this.getCampaignParameter(tracking);
        // both media codes end up in the ID, joined by ";" (encoded as %3B)
        assertTrue(campaign.contains("mc%253Dbanner.top"));
        assertTrue(campaign.contains("wt_mc%253Dnewsletter.2026"));
        assertTrue(campaign.contains("%3B"));
        tracking.close();
    }

    @Test
    public void testOncePerSessionStillSendsANewCampaignNextToATransmittedOne() {
        MappIntelligenceTracking first = this.newTracking(
            "https://shop.example/page?mc=banner.top", null, null);
        first.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));
        MappIntelligenceCookie cookie = first.getTransmittedCampaignsCookie();
        first.close();

        // banner.top was already transmitted, newsletter.2026 is new
        MappIntelligenceTracking second = this.newTracking(
            "https://shop.example/page?mc=banner.top&wt_mc=newsletter.2026", cookie.getName(), cookie.getValue());
        second.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));

        assertEquals("wt_mc%253Dnewsletter.2026", this.getCampaignParameter(second));

        MappIntelligenceCookie secondCookie = second.getTransmittedCampaignsCookie();
        assertNotNull(secondCookie);
        assertTrue(secondCookie.getValue().contains("mc%253Dbanner.top"));
        assertTrue(secondCookie.getValue().contains("wt_mc%253Dnewsletter.2026"));
        second.close();
    }

    @Test
    public void testTransmittedCampaignsCookieIsSecureOnlyOnHttps() {
        MappIntelligenceTracking https = this.newTracking(
            "https://shop.example/page?wt_mc=newsletter.2026", null, null);
        https.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));
        assertTrue(https.getTransmittedCampaignsCookie().isSecure());
        https.close();

        // a Secure cookie would never come back from a plain HTTP page
        MappIntelligenceTracking http = this.newTracking(
            "http://shop.example/page?wt_mc=newsletter.2026", null, null);
        http.track((new MappIntelligenceDataMap()).campaign(
            (new MappIntelligenceCampaign()).setOncePerSession(true)
        ));
        assertFalse(http.getTransmittedCampaignsCookie().isSecure());
        http.close();
    }
}
