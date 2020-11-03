package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingOrderTest {
    @Test
    public void testNewOrderWithoutValue() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();

        Map<String, Object> data = order.getData();
        assertEquals(0.0, data.get("value"));
    }

    @Test
    public void testNewOrderWithValue() {
        MappIntelligenceOrder order = new MappIntelligenceOrder(24.95);

        Map<String, Object> data = order.getData();
        assertEquals(24.95, data.get("value"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();

        Map<String, Object> data = order.getData();
        assertEquals(0.0, data.get("value"));
        assertEquals("", data.get("id"));
        assertEquals("", data.get("currency"));
        assertEquals(0.0, data.get("couponValue"));
        assertEquals("", data.get("paymentMethod"));
        assertEquals("", data.get("shippingService"));
        assertEquals("", data.get("shippingSpeed"));
        assertEquals(0.0, data.get("shippingCosts"));
        assertEquals(0.0, data.get("grossMargin"));
        assertEquals("", data.get("orderStatus"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
    }

    @Test
    public void testSetParameter() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setParameter(2, "foo");
        order.setParameter(15, "bar");

        Map<String, Object> data = order.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testSetValue() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setValue(24.95);

        Map<String, Object> data = order.getData();
        assertEquals(24.95, data.get("value"));
    }

    @Test
    public void testSetCouponValue() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setCouponValue(10.99);

        Map<String, Object> data = order.getData();
        assertEquals(10.99, data.get("couponValue"));
    }

    @Test
    public void testSetPaymentMethod() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setPaymentMethod("paypal");

        Map<String, Object> data = order.getData();
        assertEquals("paypal", data.get("paymentMethod"));
    }

    @Test
    public void testSetOrderStatus() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setOrderStatus("payed");

        Map<String, Object> data = order.getData();
        assertEquals("payed", data.get("orderStatus"));
    }

    @Test
    public void testSetId() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setId("ABC123");

        Map<String, Object> data = order.getData();
        assertEquals("ABC123", data.get("id"));
    }

    @Test
    public void testSetGrossMargin() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setGrossMargin(6.95);

        Map<String, Object> data = order.getData();
        assertEquals(6.95, data.get("grossMargin"));
    }

    @Test
    public void testSetShippingService() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setShippingService("dhl");

        Map<String, Object> data = order.getData();
        assertEquals("dhl", data.get("shippingService"));
    }

    @Test
    public void testSetShippingSpeed() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setShippingSpeed("2d");

        Map<String, Object> data = order.getData();
        assertEquals("2d", data.get("shippingSpeed"));
    }

    @Test
    public void testSetShippingCosts() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setShippingCosts(3.95);

        Map<String, Object> data = order.getData();
        assertEquals(3.95, data.get("shippingCosts"));
    }

    @Test
    public void testSetCurrency() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order.setCurrency("EUR");

        Map<String, Object> data = order.getData();
        assertEquals("EUR", data.get("currency"));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();
        order
                .setValue(24.95)
                .setCouponValue(10.99)
                .setPaymentMethod("paypal")
                .setOrderStatus("payed")
                .setId("ABC123")
                .setGrossMargin(6.95)
                .setShippingService("dhl")
                .setShippingSpeed("2d")
                .setShippingCosts(3.95)
                .setCurrency("EUR")
                .setParameter(2, "param2")
                .setParameter(15, "param15");

        Map<String, String> data = order.getQueryParameter();
        assertEquals("24.95", data.get("ov"));
        assertEquals("ABC123", data.get("oi"));
        assertEquals("EUR", data.get("cr"));
        assertEquals("10.99", data.get("cb563"));
        assertEquals("paypal", data.get("cb761"));
        assertEquals("dhl", data.get("cb762"));
        assertEquals("2d", data.get("cb763"));
        assertEquals("3.95", data.get("cb764"));
        assertEquals("6.95", data.get("cb765"));
        assertEquals("payed", data.get("cb766"));
        assertEquals("param2", data.get("cb2"));
        assertEquals("param15", data.get("cb15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceOrder order = new MappIntelligenceOrder();

        Map<String, String> data = order.getQueryParameter();
        assertEquals(0, data.size());
    }
}
