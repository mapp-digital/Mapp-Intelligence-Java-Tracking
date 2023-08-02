package com.mapp.intelligence.tracking.core;

import com.mapp.intelligence.tracking.*;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;

import com.mapp.intelligence.tracking.data.MappIntelligenceDataMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceParameterMap;
import com.mapp.intelligence.tracking.data.MappIntelligenceAction;
import com.mapp.intelligence.tracking.data.MappIntelligenceCampaign;
import com.mapp.intelligence.tracking.data.MappIntelligenceCustomer;
import com.mapp.intelligence.tracking.data.MappIntelligenceOrder;
import com.mapp.intelligence.tracking.data.MappIntelligencePage;
import com.mapp.intelligence.tracking.data.MappIntelligenceProduct;
import com.mapp.intelligence.tracking.data.MappIntelligenceProductCollection;
import com.mapp.intelligence.tracking.data.MappIntelligenceSession;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class MappIntelligenceTrackingTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);
    }

    @Test
    public void testTrackIdIsRequired() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackDomain("analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((new MappIntelligenceParameterMap()).add("pn", "en.page.test")));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to track data"));
    }

    @Test
    public void testTrackDomainIsRequired() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackId("111111111111111")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((new MappIntelligenceParameterMap()).add("pn", "en.page.test")));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to track data"));
    }

    @Test
    public void testEmptyData() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track(new MappIntelligenceParameterMap()));
        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testEmptyTrack() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track());
        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.+"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testTrackingIsDeactivated() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setDeactivate(true);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.flush());
        assertFalse(mappIntelligenceTracking.track((new MappIntelligenceParameterMap()).add("pn", "en.page.test")));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: Mapp Intelligence tracking is deactivated"));
    }

    @Test
    public void testCustomParameter() {
        assertEquals("ck5", MappIntelligenceCustomParameter.CUSTOM_ACTION_PARAMETER.with(5));
        assertEquals("cs2", MappIntelligenceCustomParameter.CUSTOM_SESSION_PARAMETER.with(2));
        assertEquals("cp3", MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(3));
        assertEquals("cb7", MappIntelligenceCustomParameter.CUSTOM_PRODUCT_PARAMETER.with(7));
        assertEquals("cc20", MappIntelligenceCustomParameter.CUSTOM_CAMPAIGN_PARAMETER.with(20));
        assertEquals("cg12", MappIntelligenceCustomParameter.CUSTOM_PAGE_CATEGORY.with(12));
        assertEquals("ca8", MappIntelligenceCustomParameter.CUSTOM_PRODUCT_CATEGORY.with(8));
        assertEquals("uc1", MappIntelligenceCustomParameter.CUSTOM_URM_CATEGORY.with(1));
    }

    @Test
    public void testSimpleData1() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((MappIntelligenceParameterMap) null));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(0, requests.size());
    }

    @Test
    public void testSimpleData2() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mic.setDeactivate(true);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((MappIntelligenceParameterMap) null));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(0, requests.size());
    }

    @Test
    public void testSimpleData3() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceParameterMap())
            .add(MappIntelligenceParameter.PAGE_NAME, "en.page.test")
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.+"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testSimpleData4() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceParameterMap())
            .add(MappIntelligenceParameter.PAGE_NAME, "en.page.test")
            .add(MappIntelligenceCustomParameter.CUSTOM_PAGE_CATEGORY.with(1), "page.test")
            .add(MappIntelligenceCustomParameter.CUSTOM_PAGE_CATEGORY.with(2), "en")
            .add(MappIntelligenceCustomParameter.CUSTOM_PAGE_CATEGORY.with(3), "page")
            .add(MappIntelligenceCustomParameter.CUSTOM_PAGE_CATEGORY.with(4), "test")
            .add(MappIntelligenceParameter.ORDER_VALUE, "360.93")
            .add(MappIntelligenceParameter.CUSTOMER_ID, "24")
            .add(MappIntelligenceParameter.FIRST_NAME, "John")
            .add(MappIntelligenceParameter.LAST_NAME, "Doe")
            .add(MappIntelligenceParameter.EMAIL, "john@doe.com")
            .add(MappIntelligenceCustomParameter.CUSTOM_SESSION_PARAMETER.with(1), "1")
            .add(MappIntelligenceParameter.PRODUCT_ID, "065ee2b001;085eo2f009;995ee1k906")
            .add(MappIntelligenceParameter.PRODUCT_COST, "59.99;49.99;15.99")
            .add(MappIntelligenceParameter.PRODUCT_QUANTITY, "1;5;1")
            .add(MappIntelligenceParameter.PRODUCT_STATUS, "conf")
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.*"));
        assertTrue(request.matches(".*&cg1=page\\.test.*"));
        assertTrue(request.matches(".*&cg2=en.*"));
        assertTrue(request.matches(".*&cg3=page.*"));
        assertTrue(request.matches(".*&cg4=test.*"));
        assertTrue(request.matches(".*&ov=360\\.93.*"));
        assertTrue(request.matches(".*&cd=24.*"));
        assertTrue(request.matches(".*&uc703=John.*"));
        assertTrue(request.matches(".*&uc704=Doe.*"));
        assertTrue(request.matches(".*&uc700=john%40doe\\.com.*"));
        assertTrue(request.matches(".*&cs1=1.*"));
        assertTrue(request.matches(".*&ba=065ee2b001%3B085eo2f009%3B995ee1k906.*"));
        assertTrue(request.matches(".*&co=59\\.99%3B49\\.99%3B15\\.99.*"));
        assertTrue(request.matches(".*&qn=1%3B5%3B1.*"));
        assertTrue(request.matches(".*&st=conf.*"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testDataObject1() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((MappIntelligenceDataMap) null));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(0, requests.size());
    }

    @Test
    public void testDataObject2() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mic.setDeactivate(true);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((MappIntelligenceDataMap) null));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(0, requests.size());
    }

    @Test
    public void testDataObject3() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceDataMap())
                .page(new MappIntelligencePage("en.page.test"))
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.+"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testDataObject4() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligencePage page = (new MappIntelligencePage("en.page.test"))
                .setCategory(1, "page.test")
                .setCategory(2, "en")
                .setCategory(3, "page")
                .setCategory(4, "test");

        MappIntelligenceSession session = (new MappIntelligenceSession())
                .setParameter(1, "1");

        MappIntelligenceCustomer customer = (new MappIntelligenceCustomer("24"))
                .setFirstName("John")
                .setLastName("Doe")
                .setCustomIdentifier("foo")
                .setEmail("john@doe.com");

        MappIntelligenceProduct product1 = (new MappIntelligenceProduct("065ee2b001"))
                .setCost(59.99)
                .setQuantity(1)
                .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product2 = (new MappIntelligenceProduct("085eo2f009"))
                .setCost(49.99)
                .setQuantity(5)
                .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product3 = (new MappIntelligenceProduct("995ee1k906"))
                .setCost(15.99)
                .setQuantity(1)
                .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product4 = (new MappIntelligenceProduct("abc"))
                .setCost(0).setQuantity(0)
                .setSoldOut(true)
                .setStatus(MappIntelligenceProduct.CONFIRMATION);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceDataMap())
                .action(new MappIntelligenceAction("webtrekk_ignore"))
                .page(page)
                .campaign(new MappIntelligenceCampaign("wt_mc%3Dfoo.bar"))
                .order(new MappIntelligenceOrder(360.93))
                .session(session)
                .customer(customer)
                .product((new MappIntelligenceProductCollection())
                        .add(product1).add(product2).add(product3).add(product4)
                )
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.*"));
        assertTrue(request.matches(".*&ceid=foo.*"));
        assertTrue(request.matches(".*&cg1=page\\.test.*"));
        assertTrue(request.matches(".*&cg2=en.*"));
        assertTrue(request.matches(".*&cg3=page.*"));
        assertTrue(request.matches(".*&cg4=test.*"));
        assertTrue(request.matches(".*&ov=360\\.93.*"));
        assertTrue(request.matches(".*&cd=24.*"));
        assertTrue(request.matches(".*&uc703=John.*"));
        assertTrue(request.matches(".*&uc704=Doe.*"));
        assertTrue(request.matches(".*&uc700=john%40doe\\.com.*"));
        assertTrue(request.matches(".*&cs1=1.*"));
        assertTrue(request.matches(".*&ba=065ee2b001%3B085eo2f009%3B995ee1k906.*"));
        assertTrue(request.matches(".*&co=59\\.99%3B49\\.99%3B15\\.99.*"));
        assertTrue(request.matches(".*&qn=1%3B5%3B1.*"));
        assertTrue(request.matches(".*&st=conf.*"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testDataObject5() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceDataMap())
            .action(null)
            .page(null)
            .campaign(null)
            .order(null)
            .session(null)
            .customer(null)
            .product(null)
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,0,,,,,[0-9]{13},0,,&.*"));
        assertTrue(MappIntelligenceUnitUtil.checkStatistics(request, "34"));
    }

    @Test
    public void testWithoutTemporarySessionId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligencePage page = (new MappIntelligencePage("en.page.test"))
            .setCategory(1, "page.test")
            .setCategory(2, "en")
            .setCategory(3, "page")
            .setCategory(4, "test");

        MappIntelligenceSession session = (new MappIntelligenceSession())
            .setParameter(1, "1");

        MappIntelligenceCustomer customer = (new MappIntelligenceCustomer("24"))
            .setFirstName("John")
            .setLastName("Doe")
            .setCustomIdentifier("foo")
            .setEmail("john@doe.com");

        MappIntelligenceProduct product1 = (new MappIntelligenceProduct("065ee2b001"))
            .setCost(59.99)
            .setQuantity(1)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product2 = (new MappIntelligenceProduct("085eo2f009"))
            .setCost(49.99)
            .setQuantity(5)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product3 = (new MappIntelligenceProduct("995ee1k906"))
            .setCost(15.99)
            .setQuantity(1)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product4 = (new MappIntelligenceProduct("abc"))
            .setCost(0).setQuantity(0)
            .setSoldOut(true)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceDataMap())
            .action(new MappIntelligenceAction("webtrekk_ignore"))
            .page(page)
            .campaign(new MappIntelligenceCampaign("wt_mc%3Dfoo.bar"))
            .order(new MappIntelligenceOrder(360.93))
            .session(session)
            .customer(customer)
            .product((new MappIntelligenceProductCollection())
                .add(product1).add(product2).add(product3).add(product4)
            )
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.*"));
        assertFalse(request.matches(".*&fpv=.*"));
        assertFalse(request.matches(".*&fpt=.*"));
    }

    @Test
    public void testWithTemporarySessionId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligencePage page = (new MappIntelligencePage("en.page.test"))
            .setCategory(1, "page.test")
            .setCategory(2, "en")
            .setCategory(3, "page")
            .setCategory(4, "test");

        MappIntelligenceSession session = (new MappIntelligenceSession())
            .setTemporarySessionId("abc123def456")
            .setParameter(1, "1");

        MappIntelligenceCustomer customer = (new MappIntelligenceCustomer("24"))
            .setFirstName("John")
            .setLastName("Doe")
            .setCustomIdentifier("foo")
            .setEmail("john@doe.com");

        MappIntelligenceProduct product1 = (new MappIntelligenceProduct("065ee2b001"))
            .setCost(59.99)
            .setQuantity(1)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product2 = (new MappIntelligenceProduct("085eo2f009"))
            .setCost(49.99)
            .setQuantity(5)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product3 = (new MappIntelligenceProduct("995ee1k906"))
            .setCost(15.99)
            .setQuantity(1)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        MappIntelligenceProduct product4 = (new MappIntelligenceProduct("abc"))
            .setCost(0).setQuantity(0)
            .setSoldOut(true)
            .setStatus(MappIntelligenceProduct.CONFIRMATION);

        assertTrue(mappIntelligenceTracking.track((new MappIntelligenceDataMap())
            .action(new MappIntelligenceAction("webtrekk_ignore"))
            .page(page)
            .campaign(new MappIntelligenceCampaign("wt_mc%3Dfoo.bar"))
            .order(new MappIntelligenceOrder(360.93))
            .session(session)
            .customer(customer)
            .product((new MappIntelligenceProductCollection())
                .add(product1).add(product2).add(product3).add(product4)
            )
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(1, requests.size());

        String request = requests.get(0);
        assertTrue(request.matches("^wt\\?p=600,en\\.page\\.test,,,,,[0-9]{13},0,,&.*"));
        assertTrue(request.matches(".*&fpv=abc123def456.*"));
        assertTrue(request.matches(".*&fpt=2\\.0\\.0.*"));
    }

    @Test
    public void testSetUserIdFailed1() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
                .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
                .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testSetUserIdFailed2() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackId("111111111111111")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testSetUserIdFailed3() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig())
            .setTrackDomain("analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
        assertTrue(this.outContent.toString().contains("[Mapp Intelligence]: The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to get user cookie"));
    }

    @Test
    public void testExistingSmartPixelEverId() {
        String smartPixelEverId = "2157070685656224066";
        Map<String, String> cookies = new HashMap<>();
        cookies.put("wtstp_eid", smartPixelEverId);
        cookies.put("foo", "bar");
        cookies.put("v", "%FC%F6%E4");
        cookies.put(null, null);

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setCookie(cookies);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
    }

    @Test
    public void testExistingServerEverId() {
        String serverEverId = "2157070685656224066";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wteid_111111111111111", serverEverId);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
    }

    @Test
    public void testExistingV4EverId() {
        String v4EverId = ";111111111111111|2157070685656224066";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .addCookie("wt3_eid", v4EverId);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE));
    }

    @Test
    public void testIgnoreServerSideEverIdWithMappIntelligenceTrackDomain() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.SERVER_SIDE_COOKIE));
    }

    @Test
    public void testIgnoreNotSupportedPixelVersion() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertNull(mappIntelligenceTracking.getUserIdCookie("v3", MappIntelligence.SERVER_SIDE_COOKIE));
        assertNull(mappIntelligenceTracking.getUserIdCookie("", MappIntelligence.SERVER_SIDE_COOKIE));
    }

    @Test
    public void testNullCookie() {
        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net"))
                .setCookie(null);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wtstp_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testServerSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.SERVER_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wteid_111111111111111", cookie.getName());
        assertTrue("/^8[0-9]{18}/", cookie.getValue().matches("^8[0-9]{18}"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("domain.tld", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewV4ClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^;111111111111111\\|8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testAppendV4ClientSideEverId() {
        String oldPixelEverIdCookie = "";
        oldPixelEverIdCookie += ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics.domain.tld"))
                .addCookie("wt3_eid", oldPixelEverIdCookie);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(
                cookie.getValue().matches("^" + oldPixelEverIdCookie.replaceAll("(\\|)", "\\\\$0") + ";111111111111111\\|8[0-9]{18}$")
        );
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewV5ClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V5, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^;111111111111111\\|8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testAppendV5ClientSideEverId() {
        String oldPixelEverIdCookie = "";
        oldPixelEverIdCookie += ";385255285199574|2155991359000202180#2157830383874881775";
        oldPixelEverIdCookie += ";222222222222222|2155991359353080227#2157830383339928168";
        oldPixelEverIdCookie += ";100000020686800|2155991359000202180#2156076321300417449";

        MappIntelligenceConfig mic = (new MappIntelligenceConfig("111111111111111", "analytics.domain.tld"))
                .addCookie("wt3_eid", oldPixelEverIdCookie);
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.V5, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wt3_eid", cookie.getName());
        assertTrue(
                cookie.getValue().matches("^" + oldPixelEverIdCookie.replaceAll("(\\|)", "\\\\$0") + ";111111111111111\\|8[0-9]{18}$")
        );
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testNewSmartPixelClientSideEverId() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics.domain.tld");
        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        MappIntelligenceCookie cookie = mappIntelligenceTracking.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE);

        assert cookie != null;
        assertEquals("wtstp_eid", cookie.getName());
        assertTrue(cookie.getValue().matches("^8[0-9]{18}$"));
        assertEquals(60 * 60 * 24 * 30 * 6, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertEquals("", cookie.getDomain());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    public void testDeactivateByInAndExclude() {
        MappIntelligenceConfig mic = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mic.addContainsInclude("sub.domain1.tld")
            .addContainsExclude("sub.domain1.tld")
            .setRequestURL("https://sub.domain.tld:80/path/to/page.html?foo=bar&test=123#abc");

        MappIntelligenceTracking mappIntelligenceTracking = new MappIntelligenceTracking(mic);

        assertFalse(mappIntelligenceTracking.track((new MappIntelligenceParameterMap())
            .add(MappIntelligenceParameter.PAGE_NAME, "en.page.test")
        ));

        List<String> requests = MappIntelligenceUnitUtil.getQueue(mappIntelligenceTracking);
        assertEquals(0, requests.size());
    }
}
