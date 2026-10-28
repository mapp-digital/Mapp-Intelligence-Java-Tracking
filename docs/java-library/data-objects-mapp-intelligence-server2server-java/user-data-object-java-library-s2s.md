# User

> Based on the public documentation <https://docs.mapp.com/docs/user-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

User tracking in Mapp Intelligence enables you to identify visitors based on unique customer information rather than relying solely on cookies. This ensures consistent user recognition — even when cookies are deleted, browsers are switched, or users access your website from different devices.

> [!WARNING]
> **Note on Data Privacy**
>
> Always transmit personal information in encrypted or hashed form.
>
> Mapp recommends using SHA256 or MD5 hashing to ensure compliance with data-protection standards.
>
> The library does not hash or encrypt any user data. All values (including `setEmail`, `setFirstName`, `setLastName`, `setTelephone`, `setBirthday` and the address fields) are transmitted exactly as you pass them, only URL-encoded. Hash or encrypt them yourself before passing them to the library.

User data can be sent to Mapp Intelligence after a successful login, registration, or purchase.

Each user is identified through a **Customer ID** and can optionally be enriched with **User Categories** for segmentation and analysis.

---

## Customer ID

The Customer ID serves as a unique identifier for a user.

It can be:

- a customer number from your CRM or shop system, or
- the user’s email address.

> [!NOTE]
> If an email address is used, it must be encrypted or hashed before transmission to comply with data-protection requirements. Mapp recommends using SHA256 or MD5 hashing to ensure that the email address cannot be read directly.
>
> The library does not hash the Customer ID. A plain (unhashed) email address passed to `setId` is transmitted as it is, so hash it yourself (for example with SHA256) before passing it.

---

## User Categories

User Categories capture additional attributes about a user — for example, loyalty level, subscription type, or login source. They allow you to enrich user profiles with contextual or behavioral data for deeper segmentation and analysis.

They must also be configured in Mapp Q3 before tracking. Find more information [here](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

When implementing, include the **ID number** and the **value** as a string.

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze** |
|---|---|---|---|
| **constructor** | Initializes a user object and defines the unique Customer ID. | – | *Visitors > User Relationship Management > Custom Visitor ID* |
| **setId** | Sets the Customer ID — a unique identifier from your CRM, shop system, or hashed email address. | – | *Visitors > User Relationship Management > Custom Visitor ID* |
| **setCustomIdentifier** | Defines an additional unique custom identifier for the user (optional), sent as custom everID (ceid). | – | *Visitors > User Relationship Management > Custom Visitor ID* |
| **setEmail** | Transmits the user’s email address. Hash or encrypt it before, the library transmits it unchanged. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setEmailRID** | Transmits the user’s email receiver ID for linking marketing communications with behavior data. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setEmailOptin** | Tracks whether the user has granted email permission (opt-in status). Only true is transmitted. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setFirstName** | Transmits the user’s first name. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setLastName** | Transmits the user’s last name. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setTelephone** | Transmits the user’s telephone number. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setGender** | Tracks the user’s gender (1 = male, 2 = female). | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setBirthday** | Transmits the user’s date of birth (YYYYMMDD). | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setCountry** | Transmits the user’s country. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setCity** | Transmits the user’s city. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setPostalCode** | Transmits the user’s postal code. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setStreet** | Transmits the user’s street. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setStreetNumber** | Transmits the user’s street number. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setValidation** | Enables validation for overwriting existing user category values. | – | *Visitors > User Relationship Management > [Name of User Category]* |
| **setCategory** | Adds or updates a User Category to describe additional user attributes. User Categories must be created beforehand in Mapp Intelligence. | UC > New Category | *Visitors > User Categories > [Name of Category]* |

* **Configuration Path Abbreviations:**

- **UC** = *Mapp Q3 > Configuration > Categorization > URM Categories > …*

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **constructor** / **setId** | `cd` | `CUSTOMER_ID` |
| **setCustomIdentifier** | `ceid` | `CUSTOM_EVER_ID` |
| **setEmail** | `uc700` | `EMAIL` |
| **setEmailRID** | `uc701` | `EMAIL_RID` |
| **setEmailOptin** | `uc702` (`true`) | `EMAIL_OPTIN` |
| **setFirstName** | `uc703` | `FIRST_NAME` |
| **setLastName** | `uc704` | `LAST_NAME` |
| **setTelephone** | `uc705` | `TELEPHONE` |
| **setGender** | `uc706` | `GENDER` |
| **setBirthday** | `uc707` | `BIRTHDAY` |
| **setCountry** | `uc708` | `COUNTRY` |
| **setCity** | `uc709` | `CITY` |
| **setPostalCode** | `uc710` | `POSTAL_CODE` |
| **setStreet** | `uc711` | `STREET` |
| **setStreetNumber** | `uc712` | `STREET_NUMBER` |
| **setValidation** | `uc713` (`true`) | `CUSTOMER_VALIDATION` |
| **setCategory** | `uc<ID>` | `CUSTOM_URM_CATEGORY` |

> [!NOTE]
> Empty values and the values `"0"`, `"0.0"` and `"false"` are removed before the request is sent. `setEmailOptin(false)`, `setValidation(false)` and `setGender(0)` are therefore not transmitted (an opt-out cannot be sent this way), and neither is a category with the value `"0"` or `"false"`. This is a known open issue of the current code.
>
> `null` counts as "not set": a string setter or a `setCategory` value called with `null` is not transmitted (no exception).
>
> The predefined fields use the URM category IDs 700–713. Do not use these IDs with `setCategory`, otherwise the values overwrite each other.

**Methods**

#### constructor

```java
MappIntelligenceCustomer mappCustomerData = new MappIntelligenceCustomer();
```

```java
/**
 * @param i Use this to transmit an unique identifier of the user
 */
MappIntelligenceCustomer mappCustomerData = new MappIntelligenceCustomer(String i);
```

#### setId

```java
/**
 * @param i Use this to transmit an unique identifier of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setId(String i);
```

#### setCustomIdentifier

```java
/**
 * @param c Use this to transmit an unique custom identifier of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setCustomIdentifier(String c);
```

#### setEmail

```java
/**
 * @param e Use this to transmit the e-mail address of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setEmail(String e);
```

#### setEmailRID

```java
/**
 * @param eRID Use this to transmit the e-mail receiver ID of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setEmailRID(String eRID);
```

#### setEmailOptin

```java
/**
 * @param eOptin Use this to transmit the e-mail opt-in status of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setEmailOptin(boolean eOptin);
```

#### setFirstName

```java
/**
 * @param fName Use this to transmit the first name of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setFirstName(String fName);
```

#### setLastName

```java
/**
 * @param lName Use this to transmit the last name of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setLastName(String lName);
```

#### setTelephone

```java
/**
 * @param t Use this to transmit the telephone number of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setTelephone(String t);
```

#### setGender

```java
/**
 * @param g Use this to transmit the gender of the user (1 = male | 2 = female)
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setGender(int g);
```

#### setBirthday

```java
/**
 * @param b Use this to transmit the user's date of birth (YYYYMMDD)
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setBirthday(String b);
```

#### setCountry

```java
/**
 * @param c Use this to transmit the country of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setCountry(String c);
```

#### setCity

```java
/**
 * @param c Use this to transmit the city of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setCity(String c);
```

#### setPostalCode

```java
/**
 * @param pCode Use this to transmit the postal code of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setPostalCode(String pCode);
```

#### setStreet

```java
/**
 * @param s Use this to transmit the street of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setStreet(String s);
```

#### setStreetNumber

```java
/**
 * @param sNumber Use this to transmit the street number of the user
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setStreetNumber(String sNumber);
```

#### setValidation

```java
/**
 * @param v Overwrites existing URM categorise
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setValidation(boolean v);
```

#### setCategory

```java
/**
 * The optional category (User Relation Management) can be used to categorise a customer. URM categories must be
 * created in the tool first of all.
 *
 * @param i     ID of the parameter
 * @param value Value of the parameter
 *
 * @return MappIntelligenceCustomer
 */
mappCustomerData.setCategory(int i, String value);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceCustomer mappCustomerData = new MappIntelligenceCustomer();
mappCustomerData.setId("user5684798169")
	.setEmailRID("")
	.setEmailOptin(true)
	.setGender(1)
	.setValidation(true)
	.setCategory(5, "login");

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).customer(mappCustomerData));
}
```
