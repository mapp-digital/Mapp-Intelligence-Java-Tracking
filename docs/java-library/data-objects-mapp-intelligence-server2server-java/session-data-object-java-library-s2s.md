# Session

> Based on the public documentation <https://docs.mapp.com/docs/session-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Sessions are automatically created in Mapp Intelligence when a visitor first triggers a request (for example, a page or event call). You cannot manually start or end a session via the library.

However, you can enrich existing sessions with additional information that applies to the entire visit – for example, the user’s login status, A/B test group, or device type. These details are transmitted as Session Parameters.

The session object also supports a temporary session ID, which helps link multiple requests within the same visit if no persistent cookie is available.

---

## Session Parameters

Session Parameters enrich session-level data with additional information about a visit.

They must be configured in Mapp Q3 before they can be tracked via the library. During setup, you define the data type (text or number) and other attributes for each parameter.

Mapp Intelligence provides a range of predefined Session Parameters for common use cases, such as *Login Status*, *Session Type*, or *Device Category*. Alternatively, you can create custom Session Parameters to capture completely individual session information — for example, *automatic website login*, *A/B test group assignment*, *NPS feedback*, or a *persistent shopping cart*.

For detailed setup instructions and a full overview of predefined parameters, see [How to set up custom parameters](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze** |
|---|---|---|---|
| **constructor** | Initializes a new session object. | – | – |
| **setLoginStatus** | Transmits the user’s current login status (request parameter `cs800`). | *SP > Login Status* | *Visitors > Session Parameters* |
| **setTemporarySessionId** | Assigns a temporary session ID to keep the session consistent without persistent storage. Sends the ID as `fpv` together with the type parameter `fpt=2.0.0`. | *–* | *–* |
| **setParameter** | Adds custom session parameters (`cs<ID>`) for visit-level information. | *SP > Own Configuration* | *Visitors > Session Parameters* |

* **Config Abbreviations:**

- **SP** = Mapp Q3 > Configuration > Custom Parameters > Session Parameters > Preconfigured > …

The generated request parameters are listed in [Request Parameters > Session](request-parameters-data-object-java-library-s2s.md#session).

> [!NOTE]
> - The library itself sets the session parameters `cs801` (library version) and `cs802` (tracking platform, `Java`) on every request. Values you set with `setParameter(801, …)` or `setParameter(802, …)` are overwritten.
> - Values that are `""`, `"0"`, `"0.0"` or `"false"` are not transmitted (for example `setParameter(3, "0")` or `setLoginStatus("")`). `null` counts as "not set" as well and is not transmitted (no exception).
> - `setParameter(800, …)` collides with `setLoginStatus` (both are `cs800`): if both are set, it is undefined which value is sent.
> - The type parameter `fpt=2.0.0` is only sent together with a session ID: `setTemporarySessionId("")` (or `null`) sends neither `fpv` nor `fpt`, and it removes an ID set before.

**Methods**

#### constructor

```java
MappIntelligenceSession mappSessionData = new MappIntelligenceSession();
```

#### setLoginStatus

```java
/**
 * @param lStatus Pass the current users login status here
 *
 * @return MappIntelligenceSession
 */
mappSessionData.setLoginStatus(String lStatus);
```

#### setTemporarySessionId

```java
/**
 * @param tSessionId Pass the temporary session ID here
 *
 * @return MappIntelligenceSession
 */
mappSessionData.setTemporarySessionId(String tSessionId);
```

#### setParameter

```java
/**
 * Session parameters always refer to a complete session (visit). If the value for the parameter is transmitted
 * during a visit several times, only the first or last value is evaluated, based on the configuration of the
 * Webtrekk GUI
 *
 * @param i ID of the parameter
 * @param v Value of the parameter
 *
 * @return MappIntelligenceSession
 */
mappSessionData.setParameter(int i, String v);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceSession mappSessionData = new MappIntelligenceSession();
mappSessionData.setLoginStatus("logged in")
	.setTemporarySessionId("abc123")
	.setParameter(2, "param2")
	.setParameter(15, "param15");

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).session(mappSessionData));
}
```
