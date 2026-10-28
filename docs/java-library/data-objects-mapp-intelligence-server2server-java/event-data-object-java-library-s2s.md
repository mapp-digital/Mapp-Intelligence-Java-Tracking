# Event

> Based on the public documentation <https://docs.mapp.com/docs/event-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Events track interactions on your website, such as clicks, scrolls, or other forms of content engagement.

With the server-side library, events are sent from your server, so no JavaScript is required in the visitor’s browser. The interaction itself has to be known on the server, for example because it triggers a request to your application.

Every tracking request also carries a page name: the event is assigned to the page name of a `MappIntelligencePage` in the same `MappIntelligenceDataMap`, or otherwise to the default page name derived from the request URL (see [Page](page-data-object-java-library-s2s.md)).

---

## Parameters

Event parameters enrich event data with additional website-specific information or metrics.

They must also be configured in Mapp Q3 before tracking. Find more information [here](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

When implementing, include the **ID number** and the **value** as a string.

---

## Goals

Website goals measure the success of specific actions on your website. They allow you to analyze user behavior and attribute conversions to campaigns.

Goals must be configured before they can be tracked via the library. Find more information [here](https://docs.mapp.com/docs/how-to-define-website-goals).

When configuring tracking, include the **goal ID** and the **value** as a string.

Each goal is considered achieved as soon as a value is tracked—regardless of the actual content of that value.

> [!NOTE]
> The predefined goal Order cannot be tracked within the event object.
>
> Avoid submitting values like "no" for goals such as Newsletter Registration, as this would still mark the goal as achieved.
>
> Goals are transmitted as E-Commerce Parameters (`cb<ID>`). Event goals, page goals and product or order E-Commerce Parameters share this namespace: if several objects of the same request use the same ID, only one of the values is sent.

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze** |
|---|---|---|---|
| **constructor** | Defines the initial event request and can overwrite the default event naming. | – | *Navigation > Events* |
| **setName** | Defines the unique identification of the event. | – | *Navigation > Events* |
| **setParameter** | Tracks additional event-specific information or metrics. Follow the syntax guidelines when defining parameters. | EP > Own Configuration | *Navigation > Event Parameters > [Name of Parameter]* |
| **setGoal** | Tracks website goals that are triggered by events (e.g. registrations or purchases). | EC > Own Configuration | Text: *E-Commerce > E-Commerce Parameters > [Name]* / Number: Metric |

* **Configuration Path Abbreviations:**

- **EP** = *Mapp Q3 > Configuration > Custom Parameters > Event Parameters > Preconfigured > …*
- **EC** = *Mapp Q3 > Configuration > Custom Parameters > E-Commerce Parameters > Preconfigured > …*

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **constructor** / **setName** | `ct` | `ACTION_NAME` |
| **setParameter** | `ck<ID>` | `CUSTOM_ACTION_PARAMETER` |
| **setGoal** | `cb<ID>` | `CUSTOM_PRODUCT_PARAMETER` |

> [!NOTE]
> Empty values and the values `"0"`, `"0.0"` and `"false"` are removed before the request is sent, so a parameter or goal with the value `"0"` or `"false"` is not transmitted. This is a known open issue of the current code.
>
> `null` counts as "not set": `setName(null)` or a `setParameter`/`setGoal` value `null` is not transmitted (no exception).

**Methods**

#### constructor

```java
MappIntelligenceAction mappActionData = new MappIntelligenceAction();
```

```java
/**
 * @param n Unique identification of the action
 */
MappIntelligenceAction mappActionData = new MappIntelligenceAction(String n);
```

#### setName

```java
/**
 * @param n Unique identification of the action
 *
 * @return MappIntelligenceAction
 */
mappActionData.setName(String n);
```

#### setParameter

```java
/**
 * You can use parameters to enrich analytical data with your own website-specific information and/or metrics.
 * Observe the syntax guidelines when defining parameters.
 *
 * @param id    ID of the parameter
 * @param value Value of the parameter
 *
 * @return MappIntelligenceAction
 */
mappActionData.setParameter(int id, String value);
```

#### setGoal

```java
/**
 * Needed when website goals are based on events.
 *
 * @param id    ID of the parameter
 * @param value Value of the parameter
 *
 * @return MappIntelligenceAction
 */
mappActionData.setGoal(int id, String value);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceAction mappActionData = new MappIntelligenceAction();
mappActionData.setName("foo.bar")
    .setParameter(2, "param2")
    .setParameter(15, "param15")
    .setGoal(2, "goal2")
    .setGoal(15, "goal15");

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).action(mappActionData));
}
```
