package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * To improve customer identification, you can use customer IDs instead of Mapp Intelligence's long-term cookies
 * ("eid"). The reason for this is that some users or programs automatically delete long-term cookies once a
 * session (visit) ends. Without customer IDs, returning customers will not be identified.
 *
 * Customer IDs can be passed to the library following a successful login or completed order, for example.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceCustomer extends AbstractMappIntelligenceData {
    /**
     * Use this to transmit an unique identifier of the user.
     */
    private String id = "";
    /**
     * Use this to transmit an unique custom identifier of the user.
     */
    private String customIdentifier = "";
    /**
     * Use this to transmit the e-mail address of the user.
     */
    private String email = "";
    /**
     * Use this to transmit the e-mail receiver ID of the user.
     */
    private String emailRID = "";
    /**
     * Use this to transmit the e-mail opt-in status of the user.
     */
    private boolean emailOptin;
    /**
     * Use this to transmit the first name of the user.
     */
    private String firstName = "";
    /**
     * Use this to transmit the last name of the user.
     */
    private String lastName = "";
    /**
     * Use this to transmit the telephone number of the user.
     */
    private String telephone = "";
    /**
     * Use this to transmit the gender of the user (1 = male | 2 = female).
     */
    private int gender;
    /**
     * Use this to transmit the user's date of birth (YYYYMMDD).
     */
    private String birthday = "";
    /**
     * Use this to transmit the country of the user.
     */
    private String country = "";
    /**
     * Use this to transmit the city of the user.
     */
    private String city = "";
    /**
     * Use this to transmit the postal code of the user.
     */
    private String postalCode = "";
    /**
     * Use this to transmit the street of the user.
     */
    private String street = "";
    /**
     * Use this to transmit the street number of the user.
     */
    private String streetNumber = "";
    /**
     * Overwrites existing URM categorise.
     */
    private boolean validation;
    /**
     * The optional category (User Relation Management) can be used to categorise a customer. URM categories must
     * be created in the tool first of all.
     */
    private Map<Integer, String> category = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceCustomer() {

    }

    /**
     * @param i Use this to transmit an unique identifier of the user
     */
    public MappIntelligenceCustomer(String i) {
        this.id = i;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("id", MappIntelligenceParameter.CUSTOMER_ID);
        queryList.put("customIdentifier", MappIntelligenceParameter.CUSTOM_EVER_ID);
        queryList.put("email", MappIntelligenceParameter.EMAIL);
        queryList.put("emailRID", MappIntelligenceParameter.EMAIL_RID);
        queryList.put("emailOptin", MappIntelligenceParameter.EMAIL_OPTIN);
        queryList.put("firstName", MappIntelligenceParameter.FIRST_NAME);
        queryList.put("lastName", MappIntelligenceParameter.LAST_NAME);
        queryList.put("telephone", MappIntelligenceParameter.TELEPHONE);
        queryList.put("gender", MappIntelligenceParameter.GENDER);
        queryList.put("birthday", MappIntelligenceParameter.BIRTHDAY);
        queryList.put("country", MappIntelligenceParameter.COUNTRY);
        queryList.put("city", MappIntelligenceParameter.CITY);
        queryList.put("postalCode", MappIntelligenceParameter.POSTAL_CODE);
        queryList.put("street", MappIntelligenceParameter.STREET);
        queryList.put("streetNumber", MappIntelligenceParameter.STREET_NUMBER);
        queryList.put("validation", MappIntelligenceParameter.CUSTOMER_VALIDATION);
        queryList.put("category", MappIntelligenceParameter.CUSTOM_URM_CATEGORY);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("customIdentifier", this.customIdentifier);
        data.put("email", this.email);
        data.put("emailRID", this.emailRID);
        data.put("emailOptin", this.emailOptin);
        data.put("firstName", this.firstName);
        data.put("lastName", this.lastName);
        data.put("telephone", this.telephone);
        data.put("gender", this.gender);
        data.put("birthday", this.birthday);
        data.put("country", this.country);
        data.put("city", this.city);
        data.put("postalCode", this.postalCode);
        data.put("street", this.street);
        data.put("streetNumber", this.streetNumber);
        data.put("validation", this.validation);
        data.put("category", this.category);

        return data;
    }

    /**
     * @param i Use this to transmit an unique identifier of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setId(String i) {
        this.id = i;

        return this;
    }

    /**
     * @param c Use this to transmit an unique custom identifier of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setCustomIdentifier(String c) {
        this.customIdentifier = c;

        return this;
    }

    /**
     * @param e Use this to transmit the e-mail address of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setEmail(String e) {
        this.email = e;

        return this;
    }

    /**
     * @param eRID Use this to transmit the e-mail receiver ID of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setEmailRID(String eRID) {
        this.emailRID = eRID;

        return this;
    }

    /**
     * @param eOptin Use this to transmit the e-mail opt-in status of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setEmailOptin(boolean eOptin) {
        this.emailOptin = eOptin;

        return this;
    }

    /**
     * @param fName Use this to transmit the first name of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setFirstName(String fName) {
        this.firstName = fName;

        return this;
    }

    /**
     * @param lName Use this to transmit the last name of the user
     * @return this
     */
    public MappIntelligenceCustomer setLastName(String lName) {
        this.lastName = lName;

        return this;
    }

    /**
     * @param t Use this to transmit the telephone number of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setTelephone(String t) {
        this.telephone = t;

        return this;
    }

    /**
     * @param g Use this to transmit the gender of the user (1 = male | 2 = female)
     *
     * @return this
     */
    public MappIntelligenceCustomer setGender(int g) {
        this.gender = g;

        return this;
    }

    /**
     * @param b Use this to transmit the user's date of birth (YYYYMMDD)
     *
     * @return this
     */
    public MappIntelligenceCustomer setBirthday(String b) {
        this.birthday = b;

        return this;
    }

    /**
     * @param c Use this to transmit the country of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setCountry(String c) {
        this.country = c;

        return this;
    }

    /**
     * @param c Use this to transmit the city of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setCity(String c) {
        this.city = c;

        return this;
    }

    /**
     * @param pCode Use this to transmit the postal code of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setPostalCode(String pCode) {
        this.postalCode = pCode;

        return this;
    }

    /**
     * @param s Use this to transmit the street of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setStreet(String s) {
        this.street = s;

        return this;
    }

    /**
     * @param sNumber Use this to transmit the street number of the user
     *
     * @return this
     */
    public MappIntelligenceCustomer setStreetNumber(String sNumber) {
        this.streetNumber = sNumber;

        return this;
    }

    /**
     * @param v Overwrites existing URM categorise
     *
     * @return this
     */
    public MappIntelligenceCustomer setValidation(boolean v) {
        this.validation = v;

        return this;
    }

    /**
     * The optional category (User Relation Management) can be used to categorise a customer. URM categories must be
     * created in the tool first of all.
     *
     * @param i     ID of the parameter
     * @param value Value of the parameter
     *
     * @return this
     */
    public MappIntelligenceCustomer setCategory(int i, String value) {
        this.category.put(i, value);

        return this;
    }
}
