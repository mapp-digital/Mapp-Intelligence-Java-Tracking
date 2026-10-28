# Order

> Based on the public documentation <https://docs.mapp.com/docs/order-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Mapp Intelligence enables the tracking of completed transactions as orders.

Order tracking forms the basis for all order-related analyses, such as:

- How many orders were completed?
- Which payment methods were used?
- Which orders were returned or cancelled?

---

## **Order ID and Order Value**

Each order is uniquely identified by an Order ID.

As soon as an order value is transmitted, an order is tracked. If no ID is submitted, Mapp Intelligence automatically generates one.

> [!NOTE]
> Providing your own Order ID is recommended, as it prevents duplicate counting when a confirmation page is reloaded and allows easier reconciliation with external systems.

The Order Value represents the total transaction amount and may include discounts, shipping, or packaging costs. Mapp recommends submitting the net value (excluding shipment costs) to allow better comparison with campaign costs.

---

## **Parameters**

E-Commerce Parameters enrich tracked order data with additional website-specific information or metrics.

They must be configured in Mapp Q3 before they can be tracked via the library. During setup, you define the data type (text or number) and other attributes for each parameter.

Mapp Intelligence provides a range of predefined E-Commerce Parameters for common order-related use cases, such as *Payment Method*, *Coupon Value*, and *Shipping Costs*. Alternatively, you can create custom E-Commerce Parameters to capture completely individual information — for example, shipping speed, delivery options, or special discounts.

For detailed setup instructions and a full overview of predefined parameters, see [How to set up custom parameters](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze **** |
|---|---|---|---|
| **constructor** | Initializes an order object. The overload `MappIntelligenceOrder(double v)` also sets the total order value (same as *setValue*). | – | Metric: Order Value |
| **setValue** | Sets the total order value (request parameter `ov`). Required. Without this value, no order is tracked. | – | Metric: Order Value |
| **setId** | Defines a unique order number (`oi`). Prevents duplicate counting and supports reconciliation with other systems. | – | Orders |
| **setCurrency** | Defines the order currency (ISO code, `cr`). Used for conversion to the default account currency. | SYS > Data Collection | – |
| **setCouponValue** | Transmits the value of an applied coupon (`cb563`). | EP > Coupon Value | Metric: Coupon Value |
| **setPaymentMethod** | Transmits the payment method of the order (`cb761`). | EP > Payment Method | EC Params |
| **setShippingService** | Transmits the shipping service used (`cb762`). | EP > Shipping Service | EC Params |
| **setShippingSpeed** | Transmits the selected shipping speed (`cb763`). | EP > Shipping Speed | EC Params |
| **setShippingCosts** | Transmits the shipping costs of the order (`cb764`). | EP > Shipping Costs | Metric: Shipping Costs |
| **setGrossMargin** | Transmits the gross margin / mark-up of the order (`cb765`). | EP > Gross Margin | Metric: Gross Margin |
| **setOrderStatus** | Transmits the order status (e.g. “confirmed”, “cancelled”) (`cb766`). | EP > Order Status | EC Params |
| **setParameter** | Adds custom e-commerce parameters (`cb<ID>`) to enrich order data with website-specific information. | EP > Own Configuration | EC Params |

* **Configuration Path Abbreviations:**

- **EP** = *Mapp Q3 > Configuration > Custom Parameters > E-Commerce Parameters > Preconfigured > …*
- **SYS** = *Mapp Q3 > Configuration > System Configuration > Data Collection*

** **Analysis Path Abbreviations:**

- **Orders** = *E-Commerce > Orders separately*
- **EC Params** = *E-Commerce > E-Commerce Parameters*
- **Metric:** Indicates that the value is available as a metric (e.g. Order Value, Coupon Value, Shipping Costs)

The generated request parameters are listed in [Request Parameters > Order](request-parameters-data-object-java-library-s2s.md#order).

> [!WARNING]
> **Zero values are not transmitted.** Before the request is built, the library removes every parameter whose value is `""`, `"0"`, `"0.0"` or `"false"` (`AbstractMappIntelligenceData.getQueryParameter()`). This is how unset fields are left out (all numeric fields default to `0`), but it also affects values you set explicitly:
>
> - `setValue(0)` / `new MappIntelligenceOrder(0)` produces no `ov` parameter, so an order with an order value of 0 is **not tracked** at all, although the Javadoc of `MappIntelligenceOrder` states that "0" values are permitted.
> - `setCouponValue(0)`, `setShippingCosts(0)` and `setGrossMargin(0)` are dropped as well.
> - `setParameter(i, "0")` (and `"0.0"`, `"false"`, `""`) is dropped.
>
> This is a known open issue. If you have to transmit a zero value, send the request with a [`MappIntelligenceParameterMap`](request-parameters-data-object-java-library-s2s.md#order): its values are sent as given and are not filtered.

> [!NOTE]
> Numeric values are converted with Java's `Double.toString()`: `25` is sent as `25.0`, and values of 10,000,000 or more (or below 0.001) are sent in scientific notation, e.g. `1.0E7`. `null` counts as "not set": a string setter or a `setParameter` value called with `null` is not transmitted (no exception), just like `""`.

> [!NOTE]
> Order parameters (`cb<ID>`) share their namespace with product parameters and with page and event goals: if several objects of the same request use the same ID, only one of the values is sent. The IDs 563 and 761–766 are used by the predefined setters (`setCouponValue`, `setPaymentMethod`, `setShippingService`, `setShippingSpeed`, `setShippingCosts`, `setGrossMargin`, `setOrderStatus`); do not use them with `setParameter`, otherwise it is undefined which of the two values is sent.

**Methods**

#### constructor

```java
MappIntelligenceOrder mappOrderData = new MappIntelligenceOrder();
```

```java
/**
 * @param v Saves the total order value. This property must be entered if total order values are to be tracked
 */
MappIntelligenceOrder mappOrderData = new MappIntelligenceOrder(double v);
```

#### setValue

```java
/**
 * @param v Saves the total order value. This property must be entered if total order values are to be tracked
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setValue(double v);
```

#### setId

```java
/**
 * @param i Contains a unique order number (order ID). Use of this setting ensures that no orders are counted twice
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setId(String i);
```

#### setCurrency

```java
/**
 * @param c The currency of an order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setCurrency(String c);
```

#### setCouponValue

```java
/**
 * @param cValue Contains the value of a coupon
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setCouponValue(double cValue);
```

#### setPaymentMethod

```java
/**
 * @param pMethod Use this to transmit the payment method of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setPaymentMethod(String pMethod);
```

#### setShippingService

```java
/**
 * @param sService Use this to transmit the shipping service of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setShippingService(String sService);
```

#### setShippingSpeed

```java
/**
 * @param sSpeed Use this to transmit the shipping speed of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setShippingSpeed(String sSpeed);
```

#### setShippingCosts

```java
/**
 * @param sCosts Use this to transmit the shipping costs of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setShippingCosts(double sCosts);
```

#### setGrossMargin

```java
/**
 * @param gMargin Use this to transmit the margin/mark-up of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setGrossMargin(double gMargin);
```

#### setOrderStatus

```java
/**
 * @param oStatus Use this to transmit the order status of the order
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setOrderStatus(String oStatus);
```

#### setParameter

```java
/**
 * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
 * Observe the syntax guidelines when defining parameters.
 *
 * @param i ID of the parameter
 * @param v Value of the parameter
 *
 * @return MappIntelligenceOrder
 */
mappOrderData.setParameter(int i, String v);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceOrder mappOrderData = new MappIntelligenceOrder();
mappOrderData.setValue(24.95)
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

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).order(mappOrderData));
}
```
