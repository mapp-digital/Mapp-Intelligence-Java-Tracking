package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Page requests are used to send page-related data to Mapp Intelligence; such as the page name, parameters,
 * or categories, among others.
 * <p>
 * Below you can find all available methods for page requests.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceProduct extends AbstractMappIntelligenceData {
    /**
     * Constant for product view.
     */
    public static final String VIEW = "view";
    /**
     * Constant for add product to cart.
     */
    public static final String BASKET = "add";
    /**
     * Constant for add product to cart.
     */
    public static final String ADD_TO_CART = "add";
    /**
     * Constant for remove product from cart.
     */
    public static final String DELETE_FROM_CART = "del";
    /**
     * Constant for checkout process.
     */
    public static final String CHECKOUT = "checkout";
    /**
     * Constant for order confirmation.
     */
    public static final String CONFIRMATION = "conf";
    /**
     * Constant for add product to wishlist.
     */
    public static final String ADD_TO_WISHLIST = "add-wl";
    /**
     * Constant for remove product from wishlist.
     */
    public static final String DELETE_FROM_WISHLIST = "del-wl";

    /**
     * Saves products placed in the shopping cart. This property must be entered if products are to be measured.
     * A product ID may not contain more than 110 characters.
     */
    private String id = "";
    /**
     * Contains the product price ("0" prices are allowed). If you transmit a product several times (quantity
     * property greater than 1), use the total price not the unit price.
     */
    private double cost;
    /**
     * Contains the product quantity.
     */
    private int quantity;
    /**
     * Contains states of your product (VIEW, BASKET, ADD_TO_CART, DELETE_FROM_CART, CHECKOUT,
     * CONFIRMATION, ADD_TO_WISHLIST, DELETE_FROM_WISHLIST).
     */
    private String status = VIEW;
    /**
     * Use this to transmit the variant of the product.
     */
    private String variant = "";
    /**
     * Use this to transmit the product is sold out or in stock (sold out = true, in stock = false).
     */
    private boolean soldOut;
    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     */
    private Map<Integer, String> parameter = new HashMap<>();
    /**
     * Product categories allow the grouping of products.
     */
    private Map<Integer, String> category = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceProduct() {
        this.filterQueryParameter = false;
    }

    /**
     * @param i Saves products placed in the shopping cart. This property must be entered if products are to be
     *          measured. A product ID may not contain more than 110 characters
     */
    public MappIntelligenceProduct(String i) {
        this.id = i;
        this.filterQueryParameter = false;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("id", MappIntelligenceParameter.PRODUCT_ID);
        queryList.put("cost", MappIntelligenceParameter.PRODUCT_COST);
        queryList.put("quantity", MappIntelligenceParameter.PRODUCT_QUANTITY);
        queryList.put("status", MappIntelligenceParameter.PRODUCT_STATUS);
        queryList.put("variant", MappIntelligenceParameter.PRODUCT_VARIANT);
        queryList.put("soldOut", MappIntelligenceParameter.PRODUCT_SOLD_OUT);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_PRODUCT_PARAMETER);
        queryList.put("category", MappIntelligenceParameter.CUSTOM_PRODUCT_CATEGORY);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("cost", this.cost);
        data.put("quantity", this.quantity);
        data.put("status", this.status);
        data.put("variant", this.variant);
        data.put("soldOut", this.soldOut);
        data.put("parameter", this.parameter);
        data.put("category", this.category);

        return data;
    }

    /**
     * @param i Saves products placed in the shopping cart. This property must be entered if products are to be
     *          measured. A product ID may not contain more than 110 characters
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setId(String i) {
        this.id = i;

        return this;
    }

    /**
     * @param c Contains the product price ("0" prices are allowed). If you transmit a product several times
     *          (quantity property greater than 1), use the total price not the unit price
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setCost(double c) {
        this.cost = c;

        return this;
    }

    /**
     * @param q Contains the product quantity
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setQuantity(int q) {
        this.quantity = q;

        return this;
    }

    /**
     * @param s Contains states of your product (VIEW, BASKET, ADD_TO_CART, DELETE_FROM_CART,
     *          CHECKOUT, CONFIRMATION, ADD_TO_WISHLIST, DELETE_FROM_WISHLIST)
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setStatus(String s) {
        if (s.equals(VIEW)
            || s.equals(BASKET)
            || s.equals(DELETE_FROM_CART)
            || s.equals(CHECKOUT)
            || s.equals(CONFIRMATION)
            || s.equals(ADD_TO_WISHLIST)
            || s.equals(DELETE_FROM_WISHLIST))
        {
            this.status = s;
        }

        return this;
    }

    /**
     * @param v Use this to transmit the variant of the product
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setVariant(String v) {
        this.variant = v;

        return this;
    }

    /**
     * @param sOut Use this to transmit the product is sold out or in stock (sold out = true, in stock = false)
     *
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setSoldOut(boolean sOut) {
        this.soldOut = sOut;

        return this;
    }

    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     *
     * @param i ID of the parameter
     * @param v Value of the parameter
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setParameter(int i, String v) {
        this.parameter.put(i, v);

        return this;
    }

    /**
     * Product categories allow the grouping of products.
     *
     * @param i ID of the parameter
     * @param v Value of the parameter
     * @return MappIntelligenceProduct
     */
    public MappIntelligenceProduct setCategory(int i, String v) {
        this.category.put(i, v);

        return this;
    }
}
