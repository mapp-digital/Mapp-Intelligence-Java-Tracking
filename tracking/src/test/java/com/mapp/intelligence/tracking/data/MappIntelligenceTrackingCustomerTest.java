package com.mapp.intelligence.tracking.data;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class MappIntelligenceTrackingCustomerTest {
    @Test
    public void testNewCustomerWithoutId() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();

        Map<String, Object> data = customer.getData();
        assertEquals("", data.get("id"));
    }

    @Test
    public void testNewCustomerWithId() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer("24");

        Map<String, Object> data = customer.getData();
        assertEquals("24", data.get("id"));
    }

    @Test
    public void testGetDefault() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();

        Map<String, Object> data = customer.getData();
        assertEquals("", data.get("id"));
        assertEquals("", data.get("customIdentifier"));
        assertEquals("", data.get("email"));
        assertEquals("", data.get("emailRID"));
        assertEquals(false, data.get("emailOptin"));
        assertEquals("", data.get("firstName"));
        assertEquals("", data.get("lastName"));
        assertEquals("", data.get("telephone"));
        assertEquals(0, data.get("gender"));
        assertEquals("", data.get("birthday"));
        assertEquals("", data.get("country"));
        assertEquals("", data.get("city"));
        assertEquals("", data.get("postalCode"));
        assertEquals("", data.get("street"));
        assertEquals("", data.get("streetNumber"));
        assertEquals(false, data.get("validation"));
        assertEquals(0, ((Map<Integer, String>) data.get("category")).size());
    }

    @Test
    public void testSetCustomIdentifier() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setCustomIdentifier("foo");

        Map<String, Object> data = customer.getData();
        assertEquals("foo", data.get("customIdentifier"));
    }

    @Test
    public void testSetFirstName() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setFirstName("John");

        Map<String, Object> data = customer.getData();
        assertEquals("John", data.get("firstName"));
    }

    @Test
    public void testSetLastName() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setLastName("Doe");

        Map<String, Object> data = customer.getData();
        assertEquals("Doe", data.get("lastName"));
    }

    @Test
    public void testSetStreetNumber() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setStreetNumber("4A");

        Map<String, Object> data = customer.getData();
        assertEquals("4A", data.get("streetNumber"));
    }

    @Test
    public void testSetCountry() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setCountry("Germany");

        Map<String, Object> data = customer.getData();
        assertEquals("Germany", data.get("country"));
    }

    @Test
    public void testSetPostalCode() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setPostalCode("12345");

        Map<String, Object> data = customer.getData();
        assertEquals("12345", data.get("postalCode"));
    }

    @Test
    public void testSetEmailOptin() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setEmailOptin(true);

        Map<String, Object> data = customer.getData();
        assertEquals(true, data.get("emailOptin"));
    }

    @Test
    public void testSetId() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setId("24");

        Map<String, Object> data = customer.getData();
        assertEquals("24", data.get("id"));
    }

    @Test
    public void testSetTelephone() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setTelephone("+491234567890");

        Map<String, Object> data = customer.getData();
        assertEquals("+491234567890", data.get("telephone"));
    }

    @Test
    public void testSetStreet() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setStreet("Robert-Koch-Platz");

        Map<String, Object> data = customer.getData();
        assertEquals("Robert-Koch-Platz", data.get("street"));
    }

    @Test
    public void testSetGender() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setGender(1);

        Map<String, Object> data = customer.getData();
        assertEquals(1, data.get("gender"));
    }

    @Test
    public void testSetEmailRID() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setEmailRID("ABC123-xyz789");

        Map<String, Object> data = customer.getData();
        assertEquals("ABC123-xyz789", data.get("emailRID"));
    }

    @Test
    public void testSetEmail() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setEmail("foo@bar.com");

        Map<String, Object> data = customer.getData();
        assertEquals("foo@bar.com", data.get("email"));
    }

    @Test
    public void testSetBirthday() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setBirthday("19900101");

        Map<String, Object> data = customer.getData();
        assertEquals("19900101", data.get("birthday"));
    }

    @Test
    public void testSetCity() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setCity("Berlin");

        Map<String, Object> data = customer.getData();
        assertEquals("Berlin", data.get("city"));
    }

    @Test
    public void testSetValidation() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setValidation(true);

        Map<String, Object> data = customer.getData();
        assertEquals(true, data.get("validation"));
    }

    @Test
    public void testSetCategory() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer.setCategory(2, "foo");
        customer.setCategory(15, "bar");

        Map<String, Object> data = customer.getData();
        assertEquals("foo", ((Map<Integer, String>) data.get("category")).get(2));
        assertEquals("bar", ((Map<Integer, String>) data.get("category")).get(15));
    }

    @Test
    public void testGetQueryParameter() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();
        customer
                .setCustomIdentifier("foo")
                .setFirstName("John")
                .setLastName("Doe")
                .setStreetNumber("4A")
                .setCountry("Germany")
                .setPostalCode("12345")
                .setEmailOptin(true)
                .setId("24")
                .setTelephone("+491234567890")
                .setStreet("Robert-Koch-Platz")
                .setGender(1)
                .setEmailRID("ABC123-xyz789")
                .setEmail("foo@bar.com")
                .setBirthday("19900101")
                .setCity("Berlin")
                .setValidation(true)
                .setCategory(2, "category2")
                .setCategory(15, "category15");

        Map<String, String> data = customer.getQueryParameter();
        assertEquals("24", data.get("cd"));
        assertEquals("foo", data.get("ceid"));
        assertEquals("foo@bar.com", data.get("uc700"));
        assertEquals("ABC123-xyz789", data.get("uc701"));
        assertEquals("true", data.get("uc702"));
        assertEquals("John", data.get("uc703"));
        assertEquals("Doe", data.get("uc704"));
        assertEquals("+491234567890", data.get("uc705"));
        assertEquals("1", data.get("uc706"));
        assertEquals("19900101", data.get("uc707"));
        assertEquals("Germany", data.get("uc708"));
        assertEquals("Berlin", data.get("uc709"));
        assertEquals("12345", data.get("uc710"));
        assertEquals("Robert-Koch-Platz", data.get("uc711"));
        assertEquals("4A", data.get("uc712"));
        assertEquals("true", data.get("uc713"));
        assertEquals("category2", data.get("uc2"));
        assertEquals("category15", data.get("uc15"));
    }

    @Test
    public void testGetDefaultQueryParameter() {
        MappIntelligenceCustomer customer = new MappIntelligenceCustomer();

        Map<String, String> data = customer.getQueryParameter();
        assertEquals(0, data.size());
    }
}
