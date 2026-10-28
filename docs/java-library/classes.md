# Classes

> Based on the public documentation <https://docs.mapp.com/docs/classes> (retrieved 2026-09-29), updated to the current code of this repository.

This section includes the available classes and examples.

> [!NOTE]
> `MappIntelligenceTracking` and `MappIntelligenceHybrid` carry the data of the current request (user agent, IP address, cookies, everID) and are **not thread-safe**. Create one instance per request and close it when the request is done - both classes implement `AutoCloseable`, so try-with-resources is the recommended way (see ***close***). If a second thread tracks through the same instance, the library logs a warning once - at level WARN, so with the default log level `ERROR` (and without a logger) you do not see it.

**MappIntelligenceTracking**

### Methods

#### constructor

For a complete configuration of the Mapp Intelligence Java library please check [Configuration](integration-and-configuration.md).

```java
/**
 * @param config Mapp Intelligence configuration
 */
MappIntelligenceTracking mappIntelligence = new MappIntelligenceTracking(MappIntelligenceConfig config);
```

#### track

Requests are collected in internal queues and sent at defined intervals, via ***flush*** call or when the instance is closed (see ***close***). For a complete data configuration of the Mapp Intelligence Java library, please check [Data Objects](data-objects-mapp-intelligence-server2server-java/README.md).

The return value is the ID of the request queue the request was added to (`0` to `requestQueues`, see [Configuration](integration-and-configuration.md)), or `-1` if the request was not added (tracking deactivated, track ID or track domain missing, tracking deactivated by the include/exclude configuration, `null` data, or the instance is already closed).

```java
/**
 * @return int Request queue ID
 */
mappIntelligence.track();
```

```java
/**
 * @param data Mapp Intelligence parameter map
 *
 * @return int Request queue ID
 */
mappIntelligence.track(MappIntelligenceParameterMap data);
```

```java
/**
 * @param data Mapp Intelligence data map
 *
 * @return int Request queue ID
 */
mappIntelligence.track(MappIntelligenceDataMap data);
```

#### flush

Here you can explicitly tell to send the data to the Mapp tracking servers.

The call is best effort: it only hands the queued requests to the background threads and returns immediately, it does not wait until they are sent. If a flush for a queue is already pending, the call is merged into it and `Task rejected, queue -<n>- must be processed.` is logged at level WARN (each queue accepts one running and two pending flush tasks; the pending task sends the new requests as well, nothing is lost). Use ***close*** if the requests have to be handed to the consumer before you continue.

After ***close***, `flush()` sends nothing: it logs `Flush ignored, the tracking instance is already closed.` at level WARN and calls the ***setOnFlushComplete*** callback right away.

```java
/**
 *
 */
mappIntelligence.flush();
```

#### setOnFlushComplete

Here you can define a callback method that is automatically called after each ***flush***. The first argument is the status of the request dispatch. The second argument specifies which queue was involved.

The callback is called once per request queue and flush task, and not only for ***flush***: the periodic flush of `requestInterval` and ***close*** call it as well (see [Configuration](integration-and-configuration.md)). Do not count on exactly `requestQueues + 1` calls per `flush()`: a flush that is merged into a pending one produces no callback of its own for that queue.

- The status is `false` if requests of that queue could not be delivered after all attempts, or if a batch of that queue was discarded because it can never be sent (for example a permanent client error or an oversized batch). An empty queue reports `true`.
- The callback runs on the background thread of the queue (`mapp-intelligence-queue-<n>`), several queues at the same time, so it must be thread-safe.
- A `flush()` after ***close*** calls it immediately in the calling thread, once per queue, with `true` if the queue is empty.
- A callback that references the tracking instance keeps it reachable: an instance that is never closed is then not cleaned up after garbage collection, only by ***close*** or the JVM shutdown hook.

```java
/**
 * @param callback Callback for complete flushing
 */
mappIntelligence.setOnFlushComplete(BiConsumer<Boolean, Integer> callback);
```

#### close

Sends all remaining requests, waits up to 30 seconds for them to be handed to the consumer and releases the thread pools and the file handle of the file consumer. The call is idempotent; after it, `track` returns `-1` and requests are no longer accepted (each rejected request is logged at level ERROR).

- The 30 seconds are one deadline shared by all queues and include the retries (`maxAttempt` × `attemptTimeout`) and the time the consumer needs. If it expires, `Timeout while waiting for queue -<n>- to be flushed, remaining requests were not sent.` is logged at level WARN and the consumer is closed anyway; requests that are still being sent at that moment are lost.
- If the calling thread is interrupted while `close()` waits, it stops waiting, closes the consumer and restores the interrupt flag.

A consumer that you passed in via the configuration (`setConsumer`) is not closed by the library. If your consumer implements `AutoCloseable`, closing it is up to you.

An instance that is never closed sends its remaining requests only once it has been garbage collected or the JVM terminates - there is no guarantee when that happens.

```java
/**
 *
 */
mappIntelligence.close();
```

##### Example

```java
try (MappIntelligenceTracking mappIntelligence = new MappIntelligenceTracking(mappConfig)) {
    mappIntelligence.track(dataMap);
}
```

#### getUserIdCookie

It is important to uniquely identify a user to fully benefit from Mapp Intelligence tracking capabilities. Mapp Intelligence uses the everID (eid) internally to keep the tracking information aligned to a user. You can either pass your own unique identifier or you can use the library to use an existing everID (see [Configuration](integration-and-configuration.md) → setCookie). If you track with both the server-side library and the pixel and you use the getUserIdCookie(pixelVersion, context) method, an eid cookie is returned (see [MappIntelligenceCookie](interfaces.md)), which you then returned to the client.

A cookie is only returned if the user has no everID yet; the library then generates one. It returns `null` if an everID already exists (from the cookies of the request), if the track ID or track domain is missing, or - with `MappIntelligence.SERVER_SIDE_COOKIE` - if the track domain is not an own tracking domain. Call the method before ***track***, so that the generated everID is also sent with the request. The method is thread-safe: an instance generates exactly one everID, no matter how often or from how many threads it is called; a second call returns `null`.

| Pixel version / context | Cookie name | Value | Domain |
|---|---|---|---|
| `SMART`, client side | `wtstp_eid` | the everID | – |
| `V4` / `V5`, client side | `wt3_eid` | the existing `wt3_eid` value of the request plus `;<trackId>\|<everId>` | – |
| any, `SERVER_SIDE_COOKIE` | `wteid_<trackId>` | the everID | the track domain without its first label (`track.example.com` → `example.com`) |

- All returned cookies have the max age 15,552,000 seconds (180 days), the path `/`, and are always Secure and HttpOnly. Only the server-side cookie has a domain.
- Every context other than `"3"` (`SERVER_SIDE_COOKIE`) is treated as client side. A `null` context counts as client side, a `null` pixel version like an unknown one (no cookie is returned).
- With an unknown pixel version (client side), or with `SERVER_SIDE_COOKIE` and a track domain that is not an own tracking domain (a Mapp domain such as `*.wt-eu02.net` or `*.webtrekk.net`), no cookie is returned, but the everID is still generated and sent with the following requests.

##### Options

| Name | Parameter |
|---|---|
| MappIntelligence.V4 | pixelVersion |
| MappIntelligence.V5 | pixelVersion |
| MappIntelligence.SMART | pixelVersion |
| MappIntelligence.CLIENT_SIDE_COOKIE | context |
| MappIntelligence.SERVER_SIDE_COOKIE | context |

```java
/**
 * @param pixelVersion Version ot the current pixel (v4, v5, smart)
 * @param context Cookie context (CLIENT_SIDE_COOKIE = "1", SERVER_SIDE_COOKIE = "3")
 *
 * @return MappIntelligenceCookie
 */
MappIntelligenceCookie mappCookie = mappIntelligence.getUserIdCookie(String pixelVersion, String context);
```

#### getTransmittedCampaignsCookie

Required if you use `MappIntelligenceCampaign.setOncePerSession(true)` (see [Campaign](data-objects-mapp-intelligence-server2server-java/campaign-data-object-java-library-s2s.md)). The library remembers the campaigns already transmitted in this session in the cookie `wtstp_tcc`. The method returns this cookie (see [MappIntelligenceCookie](interfaces.md)) after a new campaign was transmitted, otherwise `null`. You have to add it to the HTTP response yourself, before the response is committed - otherwise "once per session" cannot work across requests.

The cookie is a session cookie (max age `-1`) with the path `/`, not HttpOnly (the pixel reads it as well), and secure unless the request URL is plain HTTP.

- The value is URL-encoded and capped at 4000 encoded characters; if the list gets longer, the oldest campaigns are dropped.
- Once a new campaign was transmitted, the method returns the cookie on every later call of this instance.
- Only `track(MappIntelligenceDataMap)` with a campaign object evaluates "once per session"; a request sent with a `MappIntelligenceParameterMap` never changes the cookie.

```java
/**
 * @return MappIntelligenceCookie or null
 */
MappIntelligenceCookie campaignsCookie = mappIntelligence.getTransmittedCampaignsCookie();
```

##### Example

```java
MappIntelligenceCookie campaigns = mappIntelligence.getTransmittedCampaignsCookie();
if (campaigns != null) {
    Cookie cookie = new Cookie(campaigns.getName(), campaigns.getValue());
    cookie.setPath(campaigns.getPath());
    cookie.setSecure(campaigns.isSecure());
    cookie.setHttpOnly(campaigns.isHttpOnly());
    cookie.setMaxAge(campaigns.getMaxAge());
    response.addCookie(cookie);
}
```

**MappIntelligenceHybrid**

### Methods

#### constructor

For a complete configuration of the Mapp Intelligence Hybrid please check [Configuration](integration-and-configuration.md).

`MappIntelligenceHybrid` is meant for hybrid operation: your pages are generated and tracked on the server, while the pixel on the page sends its requests (for example events) to your own server instead of the Mapp track server. The hybrid forwards these pixel requests with their original query string (see ***track***).

The hybrid writes the requests into files (consumer type `FILE`), which you then send with the [Cron Job](cron-job.md): the constructor calls `setConsumerType(FILE)` on the configuration you pass in, unless its type is `CUSTOM`. This changes your configuration object: `maxBatchSize` becomes 1 and `filePath`/`filePrefix` get their defaults if they are empty. A consumer passed in via `setConsumer` is used as it is, whatever the type.

```java
/**
 * @param config Mapp Intelligence configuration
 */
MappIntelligenceHybrid mappIntelligenceHybrid = new MappIntelligenceHybrid(MappIntelligenceConfig config);
```

#### track

Reads all query parameters from the current request URL (see `setRequestURL` in [Configuration](integration-and-configuration.md)) and saves the request in the internal queues, which are sent at defined intervals, via ***flush*** call or when the instance is closed. The return value is the request queue ID, or `-1` if nothing was added (tracking deactivated, no request URL, an invalid URL or `null` passed to `track(String rURL)`, a request URL without query parameters, a query string that is not a valid pixel request, or the instance is already closed).

- Unlike `MappIntelligenceTracking`, the hybrid does not check the track ID or track domain and does not apply the include/exclude filters.
- The query string is forwarded as it is, except for the parameters that only the server may set: the client's `X-WT-IP`, `X-WT-UA` and `X-WT-SEC-CH-UA*` parameters are removed first (the parameter name is URL-decoded and compared case-insensitively), so a client cannot pretend to be another user. The library then adds user agent, IP address and client hints from the configuration, if they are set there. It does **not** add the everID (`eid`), because the pixel requests already carry their own.
- `track(String rURL)` with a malformed URL or `null` does not track anything: the request URL of the instance is reset (`null`) and `-1` is returned. The previous request URL is not tracked again.
- Only pixel requests are accepted: the query string has to contain the pixel parameter `p` with a value and may be at most 64 KB, measured in UTF-8 bytes of the query string after the server parameters were removed (see ***isValidPixelRequest***). The hybrid endpoint is public, so any other request is ignored (logged at level WARN, `-1` is returned) instead of being written into the log files.

```java
/**
 * @return int Request queue ID
 */
mappIntelligenceHybrid.track();
```

```java
/**
 * @param rURL request url with query parameters
 *
 * @return int Request queue ID
 */
mappIntelligenceHybrid.track(String rURL);
```

#### isValidPixelRequest

Checks whether a query string is accepted by ***track***: it must not be `null`, must contain the pixel parameter `p` with a value (`p=...`) and may be at most 64 KB (65,536 bytes, measured in UTF-8). ***track*** calls it with the query string after the server parameters (`X-WT-IP`, `X-WT-UA`, `X-WT-SEC-CH-UA*`) were removed.

```java
/**
 * @param queryString Query string of the pixel request
 *
 * @return true if the query string contains the pixel parameter "p" and is not too long
 */
boolean valid = MappIntelligenceHybrid.isValidPixelRequest(String queryString);
```

#### flush

Here you can explicitly tell to send the data to the Mapp tracking servers.

As with `MappIntelligenceTracking`, the call is best effort and returns immediately (merged flushes, flush after close); use ***close*** to wait for the requests.

```java
/**
 *
 */
mappIntelligenceHybrid.flush();
```

#### setOnFlushComplete

Here you can define a callback method that is automatically called after each ***flush***. The first argument is the status of the request dispatch. The second argument specifies which queue was involved. The same rules as for `MappIntelligenceTracking.setOnFlushComplete()` apply (background thread, merged flushes, calls from the interval flush and ***close***).

```java
/**
 * @param callback Callback for complete flushing
 */
mappIntelligenceHybrid.setOnFlushComplete(BiConsumer<Boolean, Integer> callback);
```

#### close

Same as `MappIntelligenceTracking.close()`: sends the remaining requests, waits up to 30 seconds (one deadline for all queues) for them to be handed to the consumer, releases thread pools and file handles, and is idempotent. Use one instance per request with try-with-resources (see the example below).

```java
/**
 *
 */
mappIntelligenceHybrid.close();
```

#### getUserIdCookie

Returns the same cookies as `MappIntelligenceTracking.getUserIdCookie()` (see above), so a user without everID gets one. The generated everID is, however, **not** added to the requests of the hybrid (see ***track***): the requests forwarded from the pixel carry their own `eid`.

```java
/**
 * @param pixelVersion Version ot the current pixel (v4, v5, smart)
 * @param context Cookie context (CLIENT_SIDE_COOKIE = "1", SERVER_SIDE_COOKIE = "3")
 *
 * @return MappIntelligenceCookie
 */
MappIntelligenceCookie mappCookie = mappIntelligenceHybrid.getUserIdCookie(String pixelVersion, String context);
```

> [!NOTE]
> `getTransmittedCampaignsCookie()` is also available on `MappIntelligenceHybrid`, but it always returns `null` there: the hybrid forwards the pixel requests unchanged and does not evaluate campaigns.

#### getResponseAsString

As response, the method returns a 1x1px transparent gif, which you then returned to the client.

> [!NOTE]
> The string is created from the GIF bytes with the platform default charset, so it does not reliably contain the original bytes. To send the image to the client, use ***getResponseAsBytes***.

```java
/**
 * @return Returns a 1x1px transparent gif as string.
 */
String imageString = mappIntelligenceHybrid.getResponseAsString();
```

#### getResponseAsBytes

As response, the method returns a 1x1px transparent gif (43 bytes), which you then returned to the client. Write the bytes to the response as they are.

> [!NOTE]
> The method returns the library's shared array, not a copy. Do not modify it.

```java
/**
 * @return Returns a 1x1px transparent gif as byte array.
 */
byte[] imageBytes = mappIntelligenceHybrid.getResponseAsBytes();
```

#### Example

```java
package demo.servlets;
import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceCookie;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/pix/*")
public class TrackingServlet extends HttpServlet {
    public TrackingServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // set tracking configuration
        MappIntelligenceConfig mappConfig = new MappIntelligenceConfig()
            .setTrackId("111111111111111")
            .setTrackDomain("analytics01.wt-eu02.net")
            .setReferrerURL(request.getHeader("Referer"))
            // with the query string: the hybrid forwards the query parameters of the pixel request
            .setRequestURL(request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""))
            .setRemoteAddress(request.getRemoteAddr())
            .setUserAgent(request.getHeader("User-Agent"));

        // read out all client cookies and add them to the configuration
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                mappConfig.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        // the instance carries the data of this request, so it is created and closed per request;
        // close() sends the remaining requests
        try (MappIntelligenceHybrid mappHybrid = new MappIntelligenceHybrid(mappConfig)) {
            // writes the incoming request to the Mapp Intelligence request queue
            mappHybrid.track();

            byte[] imgBytes = mappHybrid.getResponseAsBytes();

            // set content-type and length
            response.setContentType("image/gif");
            response.setContentLength(imgBytes.length);

            // read out the existing Mapp Intelligence user cookie
            MappIntelligenceCookie mappUserCookie = mappHybrid.getUserIdCookie(MappIntelligence.SMART, MappIntelligence.CLIENT_SIDE_COOKIE);
            if (mappUserCookie != null) {
                // create an cookie
                Cookie mappUserCookieResponse = new Cookie(mappUserCookie.getName(), mappUserCookie.getValue());
                mappUserCookieResponse.setHttpOnly(mappUserCookie.isHttpOnly());
                mappUserCookieResponse.setSecure(mappUserCookie.isSecure());
                // the v4 and smart pixel cookies have no domain (empty string)
                if (!mappUserCookie.getDomain().isEmpty()) {
                    mappUserCookieResponse.setDomain(mappUserCookie.getDomain());
                }
                mappUserCookieResponse.setPath(mappUserCookie.getPath());
                mappUserCookieResponse.setMaxAge(mappUserCookie.getMaxAge());
                // add the cookie to the response
                response.addCookie(mappUserCookieResponse);
            }

            // send an 1x1 pixel transparent gif back to the client, unchanged
            OutputStream out = response.getOutputStream();
            out.write(imgBytes);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
```

**MappIntelligenceConfig**

The configuration is described in [Integration and Configuration](integration-and-configuration.md). One method is not listed there:

### Methods

#### build

Public, but mainly used internally: the tracking classes, the consumers and the cron job call it to read the configuration. It returns the current settings as a map. It also writes the FILE defaults into the configuration (for the consumer type `FILE`: default `filePath` and `filePrefix` if they are empty, `maxBatchSize` 1), derives the own domain from the request URL if the domain list is empty (without storing it), and recomputes whether the include/exclude filters deactivate tracking.

```java
/**
 * @return Map(String, Object) Current configuration
 */
Map<String, Object> config = mappIntelligenceConfig.build();
```

**MappIntelligenceConsumerHttpClient** (package `com.mapp.intelligence.tracking.consumer`)

The built-in consumer for the consumer type `HTTP_CLIENT` (see [Data Transfer: Consumer](data-transfer-consumer.md)). The library creates it itself; create it yourself only to extend it or to share one instance via `setConsumer`.

### Methods

#### constructor

```java
/**
 * @param config Mapp Intelligence configuration, e.g. mappIntelligenceConfig.build()
 */
MappIntelligenceConsumerHttpClient consumer = new MappIntelligenceConsumerHttpClient(Map<String, Object> config);
```

#### setOnSessionId

Optional callback that receives the ID of the TLS session the batch was sent over, after each batch. It is called from the queue threads, so it must be thread-safe.

```java
/**
 * @param callback Receiver of the TLS session ID
 */
consumer.setOnSessionId(Consumer<byte[]> callback);
```

#### close

The HTTP consumer holds no resources of its own, so `close()` does nothing. Idle connections belong to the keep-alive cache of the JDK, which reuses them for the next batches and closes them after the keep-alive timeout of the server.

```java
consumer.close();
```

**MappIntelligenceConsumerFile** (package `com.mapp.intelligence.tracking.consumer`)

The built-in consumer for the consumer type `FILE` (see [Data Transfer: Consumer](data-transfer-consumer.md)). All instances of one JVM with the same file path and file prefix share one temporary file.

### Methods

#### constructor

```java
/**
 * @param config Mapp Intelligence configuration, e.g. mappIntelligenceConfig.build()
 */
MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(Map<String, Object> config);
```

#### close

Closes this consumer; the shared file is closed when the last consumer for the same path and prefix is closed. Idempotent. A consumer you created yourself and passed in via `setConsumer` has to be closed by you.

```java
consumer.close();
```

**MappIntelligenceDataMap / MappIntelligenceParameterMap / MappIntelligenceProductCollection** (package `com.mapp.intelligence.tracking.data`)

The containers passed to `track()` (see [Data Objects](data-objects-mapp-intelligence-server2server-java/README.md) and [Request Parameters](data-objects-mapp-intelligence-server2server-java/request-parameters-data-object-java-library-s2s.md)). Each has a public `build()` that returns its content; it is used by the library and returns the internal map or list, not a copy.

```java
Map<String, Object> data = dataMap.build();
Map<String, String> parameters = parameterMap.build();
List<MappIntelligenceProduct> products = productCollection.build();
```

**AbstractMappIntelligenceData** (package `com.mapp.intelligence.tracking.data`)

Base class of all data objects (`MappIntelligencePage`, `MappIntelligenceAction`, …). Useful for debugging:

### Methods

#### getData

```java
/**
 * @return Map(String, Object) All fields of the data object, including unset ones
 */
Map<String, Object> data = mappPageData.getData();
```

#### getQueryParameter

```java
/**
 * @return Map(String, String) The request parameters the object produces, after null values and
 *         (except for products) "", "0", "0.0", "false" and "{}" were removed
 */
Map<String, String> queryParameter = mappPageData.getQueryParameter();
```

For a campaign, `getQueryParameter()` contains only the explicitly set ID; the media codes of the request URL and "once per session" are resolved by `track()`.

**MappIntelligenceParameter** (package `com.mapp.intelligence.tracking`)

Constants with the names of all request parameters, e.g. `MappIntelligenceParameter.PAGE_NAME` (`pn`) or `MappIntelligenceParameter.ORDER_VALUE` (`ov`). They are listed per topic in [Request Parameters](data-objects-mapp-intelligence-server2server-java/request-parameters-data-object-java-library-s2s.md). In addition:

| Name | Value | Description |
|---|---|---|
| CUSTOM_SESSION_PARAMETER | `cs` | Prefix of session parameters. |
| CUSTOM_PAGE_PARAMETER | `cp` | Prefix of page parameters. |
| CUSTOM_PRODUCT_PARAMETER | `cb` | Prefix of e-commerce parameters (product, order, goals). |
| CUSTOM_ACTION_PARAMETER | `ck` | Prefix of event parameters. |
| CUSTOM_CAMPAIGN_PARAMETER | `cc` | Prefix of campaign parameters. |
| CUSTOM_REGISTRATION_PARAMETER | `er` | Prefix of the Engage registration parameters. |
| CUSTOM_PAGE_CATEGORY | `cg` | Prefix of page categories. |
| CUSTOM_PRODUCT_CATEGORY | `ca` | Prefix of product categories. |
| CUSTOM_URM_CATEGORY | `uc` | Prefix of user (URM) categories. |
| SMART_PIXEL_COOKIE_NAME | `wtstp_eid` | everID cookie of the Smart Pixel. |
| TRANSMITTED_CAMPAIGNS_COOKIE_NAME | `wtstp_tcc` | Cookie with the campaigns already transmitted in this session. |
| PIXEL_COOKIE_NAME | `wt3_eid` | everID cookie of pixel v4/v5. |
| SERVER_COOKIE_NAME_PREFIX | `wteid_` | Prefix of the server-side everID cookie (`wteid_<trackId>`). |

**MappIntelligenceCustomParameter** (package `com.mapp.intelligence.tracking`)

Public enum to build the names of numbered custom parameters. The constants are `CUSTOM_SESSION_PARAMETER`, `CUSTOM_PAGE_PARAMETER`, `CUSTOM_PRODUCT_PARAMETER`, `CUSTOM_ACTION_PARAMETER`, `CUSTOM_CAMPAIGN_PARAMETER`, `CUSTOM_REGISTRATION_PARAMETER`, `CUSTOM_PAGE_CATEGORY`, `CUSTOM_PRODUCT_CATEGORY` and `CUSTOM_URM_CATEGORY` (prefixes as in the table above).

#### with

```java
/**
 * @param id ID of the custom parameter
 *
 * @return String e.g. "cp5" for CUSTOM_PAGE_PARAMETER.with(5)
 */
String name = MappIntelligenceCustomParameter.CUSTOM_PAGE_PARAMETER.with(int id);
```

**MappIntelligenceLogLevel** (package `com.mapp.intelligence.tracking`)

Constants for `MappIntelligenceConfig.setLogLevel(int)`: `NONE` = 0, `FATAL` = 1, `ERROR` = 2 (default), `WARN` = 3, `INFO` = 4, `DEBUG` = 5. A level shows all messages of that level and of the levels with a lower number.

#### getName

```java
/**
 * @param ll Integer log level
 *
 * @return Log level name, e.g. "ERROR", or null for an unknown value
 */
String name = MappIntelligenceLogLevel.getName(int ll);
```

#### getValue

```java
/**
 * @param ll Log level name (case-insensitive)
 *
 * @return int Integer log level, -1 for an unknown name or null
 */
int value = MappIntelligenceLogLevel.getValue(String ll);
```
