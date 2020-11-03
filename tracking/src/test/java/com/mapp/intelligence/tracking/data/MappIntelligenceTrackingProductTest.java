package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingProductTest {
    @Test
    public void testNewProductWithoutId() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();

        Map<String, Object> data = product.getData();
        assertEquals("", data.get("id"));
    }

    @Test
    public void testNewProductWithId() {
        MappIntelligenceProduct product = new MappIntelligenceProduct("foo.bar");

        Map<String, Object> data = product.getData();
        assertEquals("foo.bar", data.get("id"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();

        Map<String, Object> data = product.getData();
        assertEquals("", data.get("id"));
        assertEquals(0.0, data.get("cost"));
        assertEquals(0, data.get("quantity"));
        assertEquals("view", data.get("status"));
        assertEquals("", data.get("variant"));
        assertEquals(false, data.get("soldOut"));
        assertEquals(0, ((Map<Integer, String>) data.get("parameter")).size());
        assertEquals(0, ((Map<Integer, String>) data.get("category")).size());
    }

    @Test
    public void testSetStatusDefault() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setStatus("wishlist");

        Map<String, Object> data = product.getData();
        assertEquals("view", data.get("status"));
    }

    @Test
    public void testSetStatusView() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setStatus(MappIntelligenceProduct.VIEW);

        Map<String, Object> data = product.getData();
        assertEquals("view", data.get("status"));
    }

    @Test
    public void testSetStatusBasket() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setStatus(MappIntelligenceProduct.BASKET);

        Map<String, Object> data = product.getData();
        assertEquals("add", data.get("status"));
    }

    @Test
    public void testSetStatusConfirmation() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setStatus(MappIntelligenceProduct.CONFIRMATION);

        Map<String, Object> data = product.getData();
        assertEquals("conf", data.get("status"));
    }

    @Test
    public void testSetParameter() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setParameter(2, "foo");
        product.setParameter(15, "bar");

        Map<String, Object> data = product.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("parameter")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("parameter")).get(15));
    }

    @Test
    public void testSetVariant() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setVariant("red");

        Map<String, Object> data = product.getData();
        assertEquals("red", data.get("variant"));
    }

    @Test
    public void testSetQuantity() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setQuantity(5);

        Map<String, Object> data = product.getData();
        assertEquals(5, data.get("quantity"));
    }

    @Test
    public void testSetId() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setId("id of a product");

        Map<String, Object> data = product.getData();
        assertEquals("id of a product", data.get("id"));
    }

    @Test
    public void testSetCategory() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setCategory(2, "foo");
        product.setCategory(15, "bar");

        Map<String, Object> data = product.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("category")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("category")).get(15));
    }

    @Test
    public void testSetSoldOut() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setSoldOut(true);

        Map<String, Object> data = product.getData();
        assertEquals(true, data.get("soldOut"));
    }

    @Test
    public void testSetCost() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product.setCost(19.95);

        Map<String, Object> data = product.getData();
        assertEquals(19.95, data.get("cost"));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();
        product
                .setStatus("add")
                .setVariant("red")
                .setQuantity(5)
                .setId("id of a product")
                .setSoldOut(true)
                .setCost(19.95)
                .setParameter(2, "parameter 2")
                .setParameter(15, "parameter 15")
                .setCategory(2, "category 2")
                .setCategory(15, "category 15");

        Map<String, String> data = product.getQueryParameter();
        assertEquals("add", data.get("st"));
        assertEquals("red", data.get("cb767"));
        assertEquals("5", data.get("qn"));
        assertEquals("id of a product", data.get("ba"));
        assertEquals("true", data.get("cb760"));
        assertEquals("19.95", data.get("co"));
        assertEquals("parameter 2", data.get("cb2"));
        assertEquals("parameter 15", data.get("cb15"));
        assertEquals("category 2", data.get("ca2"));
        assertEquals("category 15", data.get("ca15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceProduct product = new MappIntelligenceProduct();

        Map<String, String> data = product.getQueryParameter();
        assertEquals("", data.get("ba"));
        assertEquals("0.0", data.get("co"));
        assertEquals("0", data.get("qn"));
        assertEquals("view", data.get("st"));
        assertEquals("", data.get("cb767"));
        assertEquals("false", data.get("cb760"));
    }
}
