# Registration

> Based on the *User Matching* section of the public documentation <https://docs.mapp.com/docs/mapp-engage-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

`MappIntelligenceRegistration` is used for the user matching between Mapp Intelligence and [Mapp Engage](mapp-engage-data-object-java-library-s2s.md). It allows you to identify or register users in Engage by transmitting their email address and related contact attributes.

Use this functionality only if the Mapp Engage integration is enabled — it cannot be used standalone.

Typical use cases include:

- Registering new contacts in Engage when users sign up or subscribe on your website.
- Updating existing Engage contacts with information such as name, gender, or opt-in status.
- Associating website activity in Intelligence with known Engage users for automation and segmentation.

The registration object is passed with `MappIntelligenceDataMap.registration()` to `MappIntelligenceTracking.track(MappIntelligenceDataMap)`.

---

**Supported Methods**

| **Method** | **Description** | **Where to analyze** |
|---|---|---|
| **constructor** | Initializes a new registration object for user matching with Engage. | – |
| **setEmail** | Email address used to identify the user in Mapp Engage. | Mapp Engage |
| **setGroupId** | Provides the group ID for a new registration in Engage. | Mapp Engage |
| **setMode** | Defines the registration type used in Engage. Determines how new contacts are added to a group:<br>- c = **Confirmed Opt-in** – new contacts receive a welcome message via email when they are added to the group; they do not need to confirm the subscription.<br>- d = **Double Opt-in** – new contacts receive an invitation email and must accept it before they are added to the group.<br>- o = **Opt-in** – new contacts are added to the group without notification.<br>The library has no default: without `setMode`, no `er3` is sent and Engage applies its own default (opt-in). | Mapp Engage |
| **setFirstName** | First name of the contact in Engage. | Mapp Engage |
| **setLastName** | Last name of the contact in Engage. | Mapp Engage |
| **setGender** | Gender of the contact: u = undisclosed, f = female, m = male. | Mapp Engage |
| **setTitle** | Contact’s title (e.g., Mr., Ms.). | Mapp Engage |
| **setIsoCode** | ISO code of the contact, e.g. `DE`. | Mapp Engage |
| **setIsoLanguage** | ISO language of the contact, e.g. `de`. | Mapp Engage |
| **setTimezone** | Time zone of the contact, e.g. `Europe/Berlin`. | Mapp Engage |
| **setOptIn** | Indicates whether the user has given consent (true: consent, false: no consent; default: false). Only when set to true will data be sent to Engage. | Mapp Engage |

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **setEmail** | `er1` | `REGISTRATION_EMAIL` |
| **setGroupId** | `er2` | `REGISTRATION_GROUP_ID` |
| **setMode** | `er3` | `REGISTRATION_MODE` |
| **setFirstName** | `er4` | `REGISTRATION_FIRST_NAME` |
| **setLastName** | `er5` | `REGISTRATION_LAST_NAME` |
| **setGender** | `er6` | `REGISTRATION_GENDER` |
| **setTitle** | `er7` | `REGISTRATION_TITLE` |
| **setIsoCode** | `er8` | `REGISTRATION_ISO_CODE` |
| **setIsoLanguage** | `er9` | `REGISTRATION_ISO_LANGUAGE` |
| **setTimezone** | `er10` | `REGISTRATION_TIMEZONE` |
| **setOptIn** | – (gate, not sent itself) | – |

See also [Request Parameters > Mapp Engage User Matching](request-parameters-data-object-java-library-s2s.md#mapp-engage-user-matching).

> [!NOTE]
> - Without `setOptIn(true)`, the registration object produces **no** request parameters at all, whatever else is set.
> - The values are not validated. `setMode` and `setGender` accept any string, and there are no constants for the documented values (`c`/`d`/`o`, `u`/`f`/`m`); a wrong value is sent unchanged. The same applies to the format of ISO code, ISO language and time zone.
> - The email address is sent as given (URL-encoded); the library does not hash it.
> - Empty fields (the default `""`) are not sent, and neither are the values `"0"`, `"0.0"` and `"false"`. `null` counts as "not set": a setter called with `null` is not transmitted (no exception).

**Methods**

#### constructor

```java
MappIntelligenceRegistration registrationData = new MappIntelligenceRegistration();
```

#### setEmail

```java
/**
 * @param e The Email address used to identify the user in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setEmail(String e);
```

#### setGroupId

```java
/**
 * @param g Provide the group ID in case of a new registration for the user in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setGroupId(String g);
```

#### setMode

```java
/**
 * @param m Provide the registration method used to register for marketing activities.
 *          "c" (confirmed opt-in), "d" (double opt-in) or "o" (opt-in). Not validated.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setMode(String m);
```

#### setFirstName

```java
/**
 * @param f First name of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setFirstName(String f);
```

#### setLastName

```java
/**
 * @param l Last name of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setLastName(String l);
```

#### setGender

```java
/**
 * @param g Gender of the user: "u" (undisclosed), "f" (female) or "m" (male). Not validated.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setGender(String g);
```

#### setTitle

```java
/**
 * @param t The title of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setTitle(String t);
```

#### setIsoCode

```java
/**
 * @param i The ISO code of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setIsoCode(String i);
```

#### setIsoLanguage

```java
/**
 * @param i The ISO language of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setIsoLanguage(String i);
```

#### setTimezone

```java
/**
 * @param t The timezone of the user to be used in Mapp Engage.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setTimezone(String t);
```

#### setOptIn

```java
/**
 * @param o Provide information that the user consented to use their data.
 *
 * @return MappIntelligenceRegistration
 */
registrationData.setOptIn(boolean o);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);

MappIntelligenceRegistration registrationData = new MappIntelligenceRegistration();
registrationData.setEmail("john@doe.com")
    .setGroupId("abc123")
    .setMode("c")
    .setFirstName("John")
    .setLastName("Doe")
    .setGender("m")
    .setTitle("Professor")
    .setIsoCode("DE")
    .setIsoLanguage("de")
    .setTimezone("Europe/Berlin")
    .setOptIn(true);

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).registration(registrationData));
}
```
