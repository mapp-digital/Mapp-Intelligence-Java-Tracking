# Mapp Engage

> Based on the public documentation <https://docs.mapp.com/docs/mapp-engage-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Mapp Engage can be connected to your tracking setup to send **user-specific data in real time**.

This enables you to enrich customer profiles with behavioral or preference-based information and to trigger automated actions directly in Engage.

Typical use cases include:

- **Sending dynamic user attributes** to Mapp Engage – for example, preferences or behaviors such as *“Size Finder used”* – to improve segmentation and targeting.
- **Triggering a Custom Web Event** when a user interacts with a specific feature (e.g., a virtual fitting room), which can start an automation workflow in Engage.

> [!NOTE]
> To use this functionality, User Matching with Mapp Engage must be enabled.
>
> Details on setup and supported methods can be found in the Engage User Matching section below.

---

**Supported Methods**

| **Method** | **Description** | **Data Type** |
|---|---|---|
| **constructor** | Initializes the Engage data object. | – |
| **setAttributes** | Sends user-specific data to Mapp Engage, which can be stored as *Custom Profile Attributes*. These attributes can be created and managed directly in Engage. Replaces all attributes set before. | Map&lt;String, Object> |
| **addAttributes** | Adds additional user attributes to be transmitted to Mapp Engage. Works like *setAttributes*, but allows appending values to an existing set (an existing key is overwritten). | Map&lt;String, Object> |
| **setEventName** | Defines the name of the Engage *Custom Web Event* (e.g. *“Size Finder Used”*). This event can trigger automations in the Engage Whiteboard. | String |
| **setEventId** | Alternative to *eventName*. Refers to the numeric Engage *Custom Web Event ID* as defined in Engage. Typically used in advanced setups. | int |

**Transmission**

- The attributes are serialized as one JSON object and sent in the request parameter `eaj`; the event name is sent as `ecwen`, the event ID as `ecwei` (see [Request Parameters > Mapp Engage](request-parameters-data-object-java-library-s2s.md#mapp-engage)).
- Every non-null attribute value is accepted (`String`, `Integer`, `Long`, `Double`, `Boolean`, `BigDecimal`, …). It is converted with `String.valueOf()` and always serialized as a JSON **string**, e.g. `true` becomes `"true"` and `3` becomes `"3"`.
- Keys and values are JSON-escaped (quotes, backslashes and control characters), so values such as `Size "M"` produce valid JSON.
- Not transmitted: attributes with a `null` key or value, empty strings, and non-finite numbers (`NaN`, `Infinity`). If no attribute is left, no `eaj` parameter is sent.
- An empty event name and the event ID `0` (the defaults) are not sent.
- `null` is ignored: `setAttributes(null)` removes all attributes set before (like an empty map), `addAttributes(null)` does nothing, and `setEventName(null)` means no event name is sent. None of them throws an exception.

**Methods**

#### constructor

```java
MappIntelligenceEngage engageData = new MappIntelligenceEngage();
```

#### setAttributes

```java
/**
 * @param a Sends user-specific data to Mapp Engage, which can be stored as Custom Profile Attributes.
 *          These attributes can be created and managed in Engage.
 *
 * @return MappIntelligenceEngage
 */
engageData.setAttributes(Map<String, Object> a);
```

#### addAttributes

```java
/**
 * @param a Sends user-specific data to Mapp Engage, which can be stored as Custom Profile Attributes.
 *          These attributes can be created and managed in Engage.
 *
 * @return MappIntelligenceEngage
 */
engageData.addAttributes(Map<String, Object> a);
```

#### setEventName

```java
/**
 * @param e Defines the name of the Engage Custom Web Event (e.g. "Size Finder Used"). This can trigger
 *          automations in the Whiteboard.
 *
 * @return MappIntelligenceEngage
 */
engageData.setEventName(String e);
```

#### setEventId

```java
/**
 * @param e Alternative to eventName. Refers to the Engage Custom Web Event ID as defined in Engage. Mostly
 *          used for advanced setups.
 *
 * @return MappIntelligenceEngage
 */
engageData.setEventId(int e);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
Map<String, Object> attrs = new HashMap<>();
attrs.put("engage.attribute.fittingToolUsed", true);
attrs.put("engage.attribute.fittingRoomVisits", 3);

MappIntelligenceEngage mappEngageData = new MappIntelligenceEngage();
mappEngageData.setAttributes(attrs)
	.setEventName("Virtual Fitting Room Used")
	.setEventId(12345);

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
	// eaj={"engage.attribute.fittingToolUsed":"true","engage.attribute.fittingRoomVisits":"3"} (order of the keys may vary)
	mit.track((new MappIntelligenceDataMap()).engage(mappEngageData));
}
```

---

## User Matching

This feature extends the Mapp Engage integration by enabling direct user matching between Mapp Intelligence and Engage.

It allows you to identify or register users in Engage by transmitting their email address and related contact attributes (request parameters `er1` – `er10`) with the data object `MappIntelligenceRegistration`. Use this functionality only if the Mapp Engage integration is enabled — it cannot be used standalone.

All methods, the generated request parameters and an example are described on the page [Registration](registration-data-object-java-library-s2s.md).

> [!NOTE]
> Without `setOptIn(true)`, the registration object produces **no** request parameters at all, whatever else is set.
