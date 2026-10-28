# Product

> Based on the public documentation <https://docs.mapp.com/docs/product-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Mapp Intelligence enables detailed tracking of product interactions across the entire shopping journey.

Each product event is assigned a **product status** that defines the context in which a product is viewed or interacted with. This allows you to analyze user behavior from the first product impression to the final purchase.

---

## Product Status

The following product statuses are supported in Mapp Intelligence:

| **Product Status** | **Description** | **Transmitted value** |
|---|---|---|
| **VIEW** | Product viewed on a product detail page. | `view` |
| **BASKET** | Product added to a shopping cart. | `add` |
| **ADD_TO_WISHLIST** | Product added to a wishlist. | `add-wl` |
| **DELETE_FROM_WISHLIST** | Product removed from a wishlist. | `del-wl` |
| **ADD_TO_CART** | Product added to a shopping cart (same value as BASKET). | `add` |
| **DELETE_FROM_CART** | Product removed from a shopping cart. | `del` |
| **CHECKOUT** | Product moved to checkout. | `checkout` |
| **CONFIRMATION** | Product purchased. | `conf` |

The statuses are constants of `MappIntelligenceProduct` (e.g. `MappIntelligenceProduct.CONFIRMATION`). `setStatus` accepts only the transmitted values listed above, so always pass the constants: a string such as `"VIEW"` or `"CONFIRMATION"` is not accepted. An invalid value is ignored silently (no log entry) and the previous status is kept; the default status is `VIEW`.

Mapp Intelligence can automatically detect **abandoned shopping carts** when products are tracked with the relevant statuses, but no purchase confirmation follows.

---

## Categories

Product categories define fixed classifications for products and enable structured analysis.

These groups must be configured in Mapp Q3 before being tracked via the library. Find more information [here](https://docs.mapp.com/docs/how-to-set-up-categories).

When implementing, include the **ID number** and the respective **value** as a string. The value should not exceed 255 characters; additional characters are truncated.

> [!NOTE]
> Categories are assigned once to a product. If a product is tracked with a category, all subsequent product calls will also be assigned to this category. Only the first tracked value will be shown. Mapp therefore recommends implementing products and categories while embedding the library. See also [How can I replace missing values ("-") in categories?](https://docs.mapp.com/docs/how-can-i-replace-missing-values-in-categories)

---

## Parameters

E-Commerce Parameters enrich tracked product data with additional website-specific information or metrics.

They must be configured in Mapp Q3 before they can be tracked via the library. During setup, you define the data type (text or number) and other attributes for each parameter.

Mapp Intelligence provides a range of predefined E-Commerce Parameters for common product-related use cases, such as *Product Sold Out*, *Product on Sale*, and *Category*. Alternatively, you can create custom E-Commerce Parameters to capture completely individual product information — for example, product size, color, brand, or campaign-specific attributes.

For detailed setup instructions and a full overview of predefined parameters, see [How to set up custom parameters](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** |
|---|---|---|
| **constructor** | Initializes a product object. A product ID is required and may not exceed 110 characters. | – |
| **setId** | Defines the unique product identifier (e.g. SKU or article number). Required for all product measurements. | – |
| **setCost** | Sets the total price of the product. “0” values are allowed. For multiple quantities, use the total price, not the unit price. | – |
| **setQuantity** | Defines the quantity of the product. “0” values are transmitted. | – |
| **setStatus** | Defines the product status (default: VIEW). The following constants of MappIntelligenceProduct are supported:<br>- VIEW<br>- BASKET<br>- ADD_TO_CART<br>- DELETE_FROM_CART<br>- CHECKOUT<br>- CONFIRMATION<br>- ADD_TO_WISHLIST<br>- DELETE_FROM_WISHLIST<br>Invalid values are ignored silently. | – |
| **setVariant** | Transmits the variant of the product (e.g. color or size). | *EP > Product variant* |
| **setSoldOut** | Indicates whether the product is sold out (true) or in stock (false). | *EP > Product sold out* |
| **setParameter** | Adds product-specific parameters to enrich tracking data. | *EP > Own Configuration* |
| **setCategory** | Assigns a product category for structured grouping and analysis. | *PC > New Category* |

* **Configuration Path Abbreviations:**

- **EP** = *Mapp Q3 > Configuration > Custom Parameters > E-Commerce Parameters > Preconfigured > …*
- **PC** = *Mapp Q3 > Configuration > Categorization > Product Categories > …*

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **constructor** / **setId** | `ba` | `PRODUCT_ID` |
| **setCost** | `co` | `PRODUCT_COST` |
| **setQuantity** | `qn` | `PRODUCT_QUANTITY` |
| **setStatus** | `st` | `PRODUCT_STATUS` |
| **setVariant** | `cb767` | `PRODUCT_VARIANT` |
| **setSoldOut** | `cb760` (`1` = sold out, `0` = in stock) | `PRODUCT_SOLD_OUT` |
| **setParameter** | `cb<ID>` | `CUSTOM_PRODUCT_PARAMETER` |
| **setCategory** | `ca<ID>` | `CUSTOM_PRODUCT_CATEGORY` |

Unlike the other data objects, products are not filtered: `0`, `0.0` and `false` values are transmitted, and `ba`, `co`, `qn`, `st`, `cb760` and `cb767` are always part of a product request (with their default values `""`, `0.0`, `0`, `view`, `0` and `""` if not set). The cost is transmitted in Java's `double` notation via `Double.toString()` (e.g. `19.95`, `0.0`); values of 10,000,000 or more (or below 0.001) are sent in scientific notation, e.g. `1.0E7`.

> [!NOTE]
> - `null` counts as "not set": a string setter or a `setParameter`/`setCategory` value called with `null` is not transmitted (no exception). `setStatus(null)`, like any other unsupported value, is ignored and the previous status is kept.
> - The 110-character limit of the product ID is not enforced by the library; a longer ID is sent unchanged.

**Product collections**

Products are always passed in a `MappIntelligenceProductCollection` (method `add`; `add(null)` is ignored). All products of a collection are merged into one request:

- Every parameter that at least one product has is transmitted; the values of all products are joined with `;` in the order in which the products were added.
- If a product does not have a parameter or category that another product has, its value is left empty (e.g. `ca1=shoes;`).
- The values `true` and `false` are converted to `1` and `0`. This applies to all product values, including custom parameters and categories.

> [!NOTE]
> Product parameters (`cb<ID>`) share their namespace with page and event goals and with order E-Commerce Parameters. The IDs 760 and 767 are used by `setSoldOut` and `setVariant`; do not use them with `setParameter`.

**Methods**

#### constructor

```java
MappIntelligenceProduct mappProductData = new MappIntelligenceProduct();
```

```java
/**
 * @param i Saves products placed in the shopping cart. This property must be entered if products are to be
 *          measured. A product ID may not contain more than 110 characters
 */
MappIntelligenceProduct mappProductData = new MappIntelligenceProduct(String i);
```

#### setId

```java
/**
 * @param i Saves products placed in the shopping cart. This property must be entered if products are to be
 *          measured. A product ID may not contain more than 110 characters
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setId(String i);
```

#### setCost

```java
/**
 * @param c Contains the product price ("0" prices are allowed). If you transmit a product several times
 *          (quantity property greater than 1), use the total price not the unit price
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setCost(double c);
```

#### setQuantity

```java
/**
 * @param q Contains the product quantity
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setQuantity(int q);
```

#### setStatus

```java
/**
 * @param s Contains states of your product (VIEW, BASKET, ADD_TO_CART, DELETE_FROM_CART,
 *          CHECKOUT, CONFIRMATION, ADD_TO_WISHLIST, DELETE_FROM_WISHLIST)
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setStatus(String s);
```

#### setVariant

```java
/**
 * @param v Use this to transmit the variant of the product
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setVariant(String v);
```

#### setSoldOut

```java
/**
 * @param sOut Use this to transmit the product is sold out or in stock (sold out = true, in stock = false)
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setSoldOut(boolean sOut);
```

#### setParameter

```java
/**
 * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
 *
 * @param i ID of the parameter
 * @param v Value of the parameter
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setParameter(int i, String v);
```

#### setCategory

```java
/**
 * Product categories allow the grouping of products.
 *
 * @param i ID of the parameter
 * @param v Value of the parameter
 *
 * @return MappIntelligenceProduct
 */
mappProductData.setCategory(int i, String v);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceProduct mappProductData = new MappIntelligenceProduct();
mappProductData.setId("id of a product")
	.setCost(19.95)
	.setQuantity(5)
	.setStatus(MappIntelligenceProduct.VIEW)
	.setVariant("blue")
	.setSoldOut(true)
	.setParameter(2, "parameter 2")
	.setParameter(15, "parameter 15")
	.setCategory(2, "category 2")
	.setCategory(15, "category 15");

MappIntelligenceProductCollection mappProductCollection = new MappIntelligenceProductCollection();
mappProductCollection.add(mappProductData);

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).product(mappProductCollection));
}
```
