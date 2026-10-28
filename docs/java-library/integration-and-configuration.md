# Integration and Configuration

> Based on the public documentation <https://docs.mapp.com/docs/integration-and-configuration> (retrieved 2026-09-29), updated to the current code of this repository.

## Integration

### Maven

```xml
<dependency>
    <groupId>com.mapp.intelligence.tracking</groupId>
    <artifactId>tracking</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation group: 'com.mapp.intelligence.tracking', name: 'tracking', version: '1.1.0'
```

## Configuration

**Config Object**

```java
MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig()
    .setTrackId("111111111111111")
    .setTrackDomain("analytics01.wt-eu02.net")
    .setDomain(new ArrayList<String>())
    .addDomain("")
    .setUseParamsForDefaultPageName(new ArrayList<String>())
    .addUseParamsForDefaultPageName("")
    .setActivateAdvancedPermission(false)
    .setAdvancedPermissionCategory(0)

    .setContainsInclude(new ArrayList<String>())
    .addContainsInclude("")
    .setContainsExclude(new ArrayList<String>())
    .addContainsExclude("")
    .setMatchesInclude(new ArrayList<String>())
    .addMatchesInclude("")
    .setMatchesExclude(new ArrayList<String>())
    .addMatchesExclude("")

    .setLogger(null)
    .setLogLevel(MappIntelligenceLogLevel.ERROR)
    .setDeactivate(false)

    .setConsumerType(MappIntelligenceConsumerType.HTTP_CLIENT)
    .setConsumer(null)
    .setFilePath("/tmp/")
    .setFilePrefix("MappIntelligenceRequests")
    .setMaxAttempt(1)
    .setAttemptTimeout(100)
    .setConnectionTimeout(30 * 1000)
    .setReadTimeout(5 * 1000)
    .setMaxBatchSize(50)
    .setMaxQueueSize(1000)
    .setRequestQueues(2)
    .setRequestInterval(0)
    .setMaxFileLines(10000)
    .setMaxFileDuration(30 * 60 * 1000)
    .setMaxFileSize(24 * 1024 * 1024)

    .setUserAgent("")
    .setClientHintUserAgent("")
    .setClientHintUserAgentFullVersionList("")
    .setClientHintUserAgentMobile("")
    .setClientHintUserAgentModel("")
    .setClientHintUserAgentPlatform("")
    .setClientHintUserAgentPlatformVersion("")
    .setRemoteAddress("")
    .setReferrerURL("")
    .setRequestURL("")
    .setCookie(new HashMap<String, String>())
    .addCookie("", "");
```

Track ID and track domain can also be passed to the constructor directly: `new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")`.

**Methods**

| Methods | Type | Notes | Default | Mandatory |
|---|---|---|---|---|
| setTrackId | String | Enter your Mapp Intelligence track ID provided by Mapp. | empty | yes |
| setTrackDomain | String | Enter your Mapp Intelligence tracking URL. | empty | yes |
| setDomain | List&lt;String> | Specify the domains you do not want to identify as an external referrer (e.g. your subdomains). Each entry is compared with the referrer domain both literally and as a regular expression. If the list is empty, the host (plus a non-default port) of the request URL is used. | empty | no |
| addDomain | String | Specify the domain you do not want to identify as an external referrer (e.g. your subdomains). | empty | no |
| setUseParamsForDefaultPageName | List&lt;String> | Includes specific URL parameter(s) in the default page name. | empty | no |
| addUseParamsForDefaultPageName | String | Includes specific URL parameter in the default page name. | empty | no |
| setActivateAdvancedPermission | Boolean | Enable if you need to differentiate between additional usage options for the tracked data, e.g. only to your Intelligence instance or to both Intelligence and Engage. | false | no |
| setAdvancedPermissionCategory | Integer | If activateAdvancedPermission is activated:<br>- 0 (default): only Intelligence<br>- 3: Intelligence and Engage<br>The value is not validated. | 0 | no |
| setContainsInclude | List&lt;String> | Specify the strings that must be contained in the request URL to measure the request. | empty | no |
| addContainsInclude | String | Specify the string that must be contained in the request URL to measure the request. | empty | no |
| setContainsExclude | List&lt;String> | Specify the strings that must be contained in the request URL to not measure the request. | empty | no |
| addContainsExclude | String | Specify the string that must be contained in the request URL to not measure the request. | empty | no |
| setMatchesInclude | List&lt;String> | Specify the regular expressions that must match the request URL to measure the request (a partial match is enough). An invalid expression is logged as an error and ignored. | empty | no |
| addMatchesInclude | String | Specify the regular expression that must match the request URL to measure the request. | empty | no |
| setMatchesExclude | List&lt;String> | Specify the regular expressions that must match the request URL to not measure the request. | empty | no |
| addMatchesExclude | String | Specify the regular expression that must match the request URL to not measure the request. | empty | no |
| setLogger | MappIntelligenceLogger | Activates the debug mode. The debug mode sends messages to your own logger class (see [MappIntelligenceLogger](interfaces.md)). Without a logger, nothing is logged. `null` keeps the current logger. | null | no |
| setLogLevel | String or int | Specify the debug log level, either as name (case-insensitive) or as `MappIntelligenceLogLevel` constant (0 to 5). An unknown value (and `null`) is silently ignored:<br>- *NONE: no logs*<br>- *FATAL: fatal errors*<br>- *ERROR*: errors leading to data loss or break code<br>- *WARN: errors that can affect data quality*<br>- *INFO:*  general info on configuration and send status<br>- *DEBUG*: general info on request sendout and queue status | ERROR | no |
| setDeactivate | Boolean | Deactivate the tracking functionality. | false | no |
| setConsumerType | String | Specify the [consumer](data-transfer-consumer.md) to use for data transfer to Intelligence ( *HTTP_CLIENT*, *FILE* or *CUSTOM*, see `MappIntelligenceConsumerType`). The string is not validated: with an unknown type, or with *CUSTOM* but without `setConsumer`, no consumer exists - every flush logs `Cannot send because no consumer is available or an undefined consumer type was specified.` at error level and the requests stay in the queue forever. The constructor of `MappIntelligenceHybrid` sets every type except *CUSTOM* to *FILE* on the configuration you pass in (see [Classes](classes.md)). | HTTP_CLIENT | no |
| setConsumer | MappIntelligenceConsumer | Specify the custom consumer to use for data transfer to Intelligence (see [MappIntelligenceConsumer](interfaces.md)). If set, it is used for sending regardless of the consumer type. It is not closed by the library. | null | only for CUSTOM |
| setFilePath | String | Enter the directory of your request logging files, with or without a trailing path separator. This is only relevant when using file consumer. | System.getProperty("java.io.tmpdir") + "/" | no |
| setFilePrefix | String | Enter the prefix from your request logging files. This is only relevant when using file consumer. | MappIntelligenceRequests | no |
| setMaxAttempt | Integer | Specify the number of send attempts per flush (1 to 5). After the maxAttempts have been reached, the requests that could not be sent **stay in the queue** and are sent again with the next flush or when the instance is closed. Values outside the range are silently ignored. | `1` | no |
| setAttemptTimeout | Integer | Specify the interval of request resend in milliseconds (1 to 500). Values outside the range are silently ignored. | 100 | no |
| setConnectionTimeout | Integer | Specify the number for connection timeout in milliseconds (HTTP_CLIENT only). 0 means no timeout; negative values are silently ignored. | 30 * 1000 | no |
| setReadTimeout | Integer | Specify the number for read timeout in milliseconds (HTTP_CLIENT only). 0 means no timeout; negative values are silently ignored. | 5 * 1000 | no |
| setMaxBatchSize | Integer | Specify the maximum request number per batch. The value is not validated: use a value from 1 to 10,000 (a value of 0 or below stops sending, a batch with more than 10,000 requests is discarded). For the FILE consumer the library always uses 1 (an explicitly set other value is logged at level DEBUG). | 50 | no |
| setMaxQueueSize | Integer | Specify the maximum number of requests saved in each queue. When a queue reaches it, the queue is sent; if that fails, the oldest requests are discarded (logged at level WARN). `0` (or a negative value) means unlimited. The cron job always uses an unlimited queue, unsent requests stay in the log file. | 1000 | no |
| setRequestInterval | Integer | Here you can define the interval in seconds at which the tracking requests should be sent automatically. If set to 0, no interval is started – sending the collected requests must then be triggered manually via flush (or happens when the instance is closed). Negative values are silently ignored. | 0 | no |
| setRequestQueues | Integer | Here you can define the number of request queues to which the requests are to be distributed evenly (by everID, or by user agent and IP address). The library creates one additional fallback queue for requests without either, so the default results in 3 queues, each with its own background thread. 0 means a single queue; negative values are silently ignored. | 2 | no |
| setMaxFileLines | Integer | The maximum number of lines per file (1 to 10,000). A larger value is reduced to 10,000 and logged at level WARN with the next build, a value below 1 is ignored. | 10 * 1000 | no |
| setMaxFileDuration | Integer | The maximum file duration in milliseconds (1 to 30 * 60 * 1000). A larger value is reduced to the maximum and logged at level WARN with the next build, a value below 1 is ignored. | 30 * 60 * 1000 | no |
| setMaxFileSize | Integer | The maximum file size in bytes (1 to 24 * 1024 * 1024). A larger value is reduced to the maximum and logged at level WARN with the next build, a value below 1 is ignored. | 24 * 1024 * 1024 | no |
| setForceSSL | Boolean | **Deprecated**, the value is ignored: every request is sent via SSL (`https://`). Will be removed in a future version. | true | no |
| setUserAgent | String | Enter the origin client HTTP user agent string. The value is URL-decoded (this applies to the client hints, the remote address and the cookies as well). | empty | no |
| setClientHintUserAgent | String | Enter the origin Client Hint header for "sec-ch-ua" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setClientHintUserAgentFullVersionList | String | Enter the origin Client Hint header for "sec-ch-ua-full-version-list" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setClientHintUserAgentMobile | String | Enter the origin Client Hint header for "sec-ch-ua-mobile" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setClientHintUserAgentModel | String | Enter the origin Client Hint header for "sec-ch-ua-model" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setClientHintUserAgentPlatform | String | Enter the origin Client Hint header for "sec-ch-ua-platform" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setClientHintUserAgentPlatformVersion | String | Enter the origin Client Hint header for "sec-ch-ua-platform-version" (see [How can I make my website ready for Google Chrome's Client Hints?](https://docs.mapp.com/docs/how-can-i-make-my-website-ready-for-google-chromes-client-hints) and [Information on Google Chrome User-Agent Client Hints](https://docs.mapp.com/docs/information-on-google-chrome-user-agent-client-hints) for more details). | empty | no |
| setRemoteAddress | String | Enter the origin client Remote address (ip). | empty | no |
| setReferrerURL | String | Enter the origin client HTTP referrer URL. The value is URL-decoded when the request is built (to compare it with `setDomain`) and encoded again for the request. | empty | no |
| setRequestURL | String | Enter the origin client HTTP request URL. An invalid URL is silently ignored. The request URL is required for the include/exclude filters. | empty | no |
| setCookie | Map&lt;String, String> | Enter a map of all origin client cookies. The cookies are added to the cookies already set. | empty | no |
| addCookie | String | Enter a origin client cookie (key, value). | empty | no |

> [!NOTE]
> - **Include/exclude filters:** a request is measured if no include filter is set, or if at least one `containsInclude` string or `matchesInclude` expression matches the request URL. It is then not measured if any `containsExclude` string or `matchesExclude` expression matches. The filters are only evaluated if a request URL is set, and they are evaluated when the tracking instance is created - set the request URL and the filters before creating it. The result is recomputed for every new instance, so a reused configuration from which all filters were removed activates tracking again.
> - **Invalid values are silently ignored:** setters with a value range keep their previous value if the new value is out of range, without a log entry. The ranges are listed in the table above. Setters that take an object or a list keep their previous value when passed `null`, and so does `setLogLevel((String) null)`.
> - **Lists are copied:** all list setters (`setDomain`, `setUseParamsForDefaultPageName`, `setContainsInclude`, `setContainsExclude`, `setMatchesInclude`, `setMatchesExclude`) store a modifiable copy of the list you pass. Immutable lists (e.g. `Arrays.asList(...)`, `List.of(...)`) are fine, the `add...()` methods work afterwards, and later changes to your own list have no effect on the configuration.
> - **Not thread-safe:** the configuration object is not thread-safe, and creating a tracking instance modifies it: for the FILE consumer the default file path and prefix and the batch size 1 are written into it (and `MappIntelligenceHybrid` sets the consumer type to FILE, see [Classes](classes.md)). The own domain is not stored: with an empty domain list it is derived from the current request URL every time an instance is created, so a reused configuration does not keep the host of its first request. The configuration's internal lists and the cookie map are handed to the instance without copying. Build one configuration per request, or do not change a shared one while instances are created from it or still in use.

**Using the configuration**

Create one tracking instance per request and close it when the request is done (see [Lifecycle and Threading](README.md#lifecycle-and-threading)):

```java
try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(mappIntelligenceConfig)) {
    tracking.track(dataMap);
}
```

**Config File (properties)**

The configuration can also be read from a file. The path is a file system path (not a class path resource); a file whose name ends with `.xml` is read as XML properties file, every other file as `.properties` file.

```java
MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(
    "path/to/your/properties/file/config.properties"
);
```

```properties
# [tracking]
tracking.trackId=111111111111111
tracking.trackDomain=analytics01.wt-eu02.net
tracking.deactivate=false
tracking.domain=
tracking.logLevel=DEBUG
tracking.useParamsForDefaultPageName=
tracking.containsInclude=foo;bar
tracking.containsExclude=test
tracking.matchesInclude=.*foo.*;.*bar.*
tracking.matchesExclude=.*test.*

# [consumer]
consumer.consumerType=FILE
consumer.filePath=
consumer.filePrefix=
consumer.maxAttempt=1
consumer.attemptTimeout=100
consumer.maxBatchSize=50
consumer.maxQueueSize=1000
consumer.maxFileLines=10000
consumer.maxFileDuration=1800000
consumer.maxFileSize=25165824
```

**Config File (XML)**

```java
MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(
    "path/to/your/xml/file/config.xml"
);
```

```xml
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <!-- [tracking] -->
    <entry key="tracking.trackId">111111111111111</entry>
    <entry key="tracking.trackDomain">analytics01.wt-eu02.net</entry>
    <entry key="tracking.deactivate">false</entry>
    <entry key="tracking.domain" />
    <entry key="tracking.logLevel">DEBUG</entry>
    <entry key="tracking.useParamsForDefaultPageName" />
    <entry key="tracking.containsInclude">foo;bar</entry>
    <entry key="tracking.containsExclude">test</entry>
    <entry key="tracking.matchesInclude">.*foo.*;.*bar.*</entry>
    <entry key="tracking.matchesExclude">.*test.*</entry>

    <!-- [consumer] -->
    <entry key="consumer.consumerType">FILE</entry>
    <entry key="consumer.filePath" />
    <entry key="consumer.filePrefix" />
    <entry key="consumer.maxAttempt">1</entry>
    <entry key="consumer.attemptTimeout">100</entry>
    <entry key="consumer.maxBatchSize">50</entry>
    <entry key="consumer.maxQueueSize">1000</entry>
    <entry key="consumer.maxFileLines">10000</entry>
    <entry key="consumer.maxFileDuration">1800000</entry>
    <entry key="consumer.maxFileSize">25165824</entry>
</properties>
```

**Notes on config files**

- `tracking.domain` and `tracking.useParamsForDefaultPageName` are split at `,`; `tracking.containsInclude`, `tracking.containsExclude`, `tracking.matchesInclude` and `tracking.matchesExclude` are split at `;`.
- Boolean values are only `true` if the value is exactly `true`; any other non-empty value means `false`. Integer values must consist of digits only, otherwise the default is used (also for values larger than `Integer.MAX_VALUE`); they are then subject to the same ranges as the setters. `tracking.logLevel` takes a level name (case-insensitive).
- `consumer.consumerType` only accepts `FILE` and `HTTP_CLIENT` (case-sensitive). Any other value, including `CUSTOM` or `file`, silently falls back to `HTTP_CLIENT`. A custom consumer (and a logger) can only be set in code.
- The following options have **no** config file key and can only be set in code: `requestQueues`, `requestInterval`, `connectionTimeout`, `readTimeout`, `activateAdvancedPermission`, `advancedPermissionCategory`, `logger`, `consumer`, as well as the request data (`userAgent`, client hints, `remoteAddress`, `referrerURL`, `requestURL`, `cookie`).
- If the file cannot be found or read, or the path is `null` (`new MappIntelligenceConfig((String) null)`), no error is reported: the configuration silently keeps its defaults.
- List entries are trimmed and empty entries are dropped (`foo; bar;;` becomes `foo`, `bar`). The lists are modifiable, so `addDomain()`, `addContainsInclude()` etc. can extend a list that was read from the file.
