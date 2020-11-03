package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapp Intelligence can also track orders. To do this, the order value is transmitted along with the order number.
 * "0" values are permitted. A list of tracked orders can be analyzed in Mapp Intelligence under "E-Commerce >
 * Orders separately".
 *
 * The difference to product tracking is that the information refers to the total order value that is transmitted
 * rather than the individual products. Besides the total of purchased products, the total order value may also
 * contain discounts, shipping, and packaging cost information.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceOrder extends AbstractMappIntelligenceData {
    /**
     * Saves the total order value. This property must be entered if total order values are to be tracked.
     */
    private double value;
    /**
     * Contains a unique order number (order ID). Use of this setting ensures that no orders are counted twice.
     */
    private String id = "";
    /**
     * The currency of an order.
     */
    private String currency = "";
    /**
     * Contains the value of a coupon.
     */
    private double couponValue;
    /**
     * Use this to transmit the payment method of the order.
     */
    private String paymentMethod = "";
    /**
     * Use this to transmit the shipping service of the order.
     */
    private String shippingService = "";
    /**
     * Use this to transmit the shipping speed of the order.
     */
    private String shippingSpeed = "";
    /**
     * Use this to transmit the shipping costs of the order.
     */
    private double shippingCosts;
    /**
     * Use this to transmit the margin/mark-up of the order.
     */
    private double grossMargin;
    /**
     * Use this to transmit the order status of the order.
     */
    private String orderStatus = "";
    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     * Observe the syntax guidelines when defining parameters.
     */
    private Map<Integer, String> parameter = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceOrder() {

    }

    /**
     * @param v Saves the total order value. This property must be entered if total order values are to be tracked
     */
    public MappIntelligenceOrder(double v) {
        this.value = v;
    }

    /**
     * @return List of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("value", MappIntelligenceParameter.ORDER_VALUE);
        queryList.put("id", MappIntelligenceParameter.ORDER_ID);
        queryList.put("currency", MappIntelligenceParameter.CURRENCY);
        queryList.put("couponValue", MappIntelligenceParameter.COUPON_VALUE);
        queryList.put("paymentMethod", MappIntelligenceParameter.PAYMENT_METHOD);
        queryList.put("shippingService", MappIntelligenceParameter.SHIPPING_SERVICE);
        queryList.put("shippingSpeed", MappIntelligenceParameter.SHIPPING_SPEED);
        queryList.put("shippingCosts", MappIntelligenceParameter.SHIPPING_COSTS);
        queryList.put("grossMargin", MappIntelligenceParameter.GROSS_MARGIN);
        queryList.put("orderStatus", MappIntelligenceParameter.ORDER_STATUS);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_PRODUCT_PARAMETER);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("value", this.value);
        data.put("id", this.id);
        data.put("currency", this.currency);
        data.put("couponValue", this.couponValue);
        data.put("paymentMethod", this.paymentMethod);
        data.put("shippingService", this.shippingService);
        data.put("shippingSpeed", this.shippingSpeed);
        data.put("shippingCosts", this.shippingCosts);
        data.put("grossMargin", this.grossMargin);
        data.put("orderStatus", this.orderStatus);
        data.put("parameter", this.parameter);

        return data;
    }

    /**
     * @param v Saves the total order value. This property must be entered if total order values are to be tracked
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setValue(double v) {
        this.value = v;

        return this;
    }

    /**
     * @param i Contains a unique order number (order ID). Use of this setting ensures that no orders are counted twice
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setId(String i) {
        this.id = i;

        return this;
    }

    /**
     * @param c The currency of an order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setCurrency(String c) {
        this.currency = c;

        return this;
    }

    /**
     * @param cValue Contains the value of a coupon
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setCouponValue(double cValue) {
        this.couponValue = cValue;

        return this;
    }

    /**
     * @param pMethod Use this to transmit the payment method of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setPaymentMethod(String pMethod) {
        this.paymentMethod = pMethod;

        return this;
    }

    /**
     * @param sService Use this to transmit the shipping service of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setShippingService(String sService) {
        this.shippingService = sService;

        return this;
    }

    /**
     * @param sSpeed Use this to transmit the shipping speed of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setShippingSpeed(String sSpeed) {
        this.shippingSpeed = sSpeed;

        return this;
    }

    /**
     * @param sCosts Use this to transmit the shipping costs of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setShippingCosts(double sCosts) {
        this.shippingCosts = sCosts;

        return this;
    }

    /**
     * @param gMargin Use this to transmit the margin/mark-up of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setGrossMargin(double gMargin) {
        this.grossMargin = gMargin;

        return this;
    }

    /**
     * @param oStatus Use this to transmit the order status of the order
     *
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setOrderStatus(String oStatus) {
        this.orderStatus = oStatus;

        return this;
    }

    /**
     * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
     * Observe the syntax guidelines when defining parameters.
     *
     * @param i ID of the parameter
     * @param v Value of the parameter
     * @return MappIntelligenceOrder
     */
    public MappIntelligenceOrder setParameter(int i, String v) {
        this.parameter.put(i, v);

        return this;
    }
}
