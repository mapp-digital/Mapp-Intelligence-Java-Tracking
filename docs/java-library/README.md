# Java Library

> Based on the public documentation <https://docs.mapp.com/docs/java-library> (retrieved 2026-09-29), updated to the current code of this repository.

The Mapp Intelligence Java Library provides a server-side integration for sending tracking data directly from your backend to Mapp Intelligence. It enables full or partial server-side tracking — for example, to record orders, confirmations, or other events without relying on client-side scripts.

Before using the Java Library, make sure you have read the [Server-to-Server Basics](https://docs.mapp.com/docs/server-to-server) section. That page explains the general tracking structure, request format, parameters, and session handling logic that apply to all server-to-server integrations.

---

## Overview

| **Aspect** | **Description** |
|---|---|
| Library name | com.mapp.intelligence.tracking |
| Purpose | Server-side data collection for Mapp Intelligence |
| Use cases | Product, order, shipment, and application tracking |
| Integration | Maven or Gradle (see [Integration & Configuration](integration-and-configuration.md)) |

---

## Key Concepts

The Java library can be used in different tracking setups depending on your data protection requirements and system architecture. Each approach determines how data is collected and forwarded to Mapp Intelligence.

---

### 1. Pure Server-Side Tracking

All tracking requests are created and sent from your server using the Java library. This approach does not rely on any client-side scripts or cookies and is ideal for environments with strict data protection or limited browser access.

Key Features:

- All consumer types are supported (FILE, HTTP_CLIENT, or custom).
- A unique user identifier (everID) must be passed manually (via `getUserIdCookie(pixelVersion, context)` or a custom ID).
- Tracking data is sent directly to the track server without browser dependencies.
- Preloading or caching effects may influence page impression counts.

#### Tracking Considerations

Pure server-side tracking or the use of the server-side library to track all page requests might affect the number of page impressions if one of the following conditions is true:

**Preloading of pages in search results**

Some browsers (for example, Safari or Google Chrome) preload websites in the background when they appear in search results. Since this generates a server call, a page impression may be counted even if the user never opened the page. This can lead to increased page impressions, higher bounce rates, and shorter visit durations.

**Caching of pages**

If pages are cached in the browser to improve performance, a page impression may not be tracked if the cache prevents a new server call. This can slightly reduce the total number of tracked page impressions and may affect path analyses for previous and following pages.

#### Configuration Example:

```java
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.FILE);
```

---

### 2. Hybrid Tracking (Pixel + Java Library)

Combining the Java library with the Mapp tracking pixel allows you to use both client- and server-side data sources. This setup provides maximum flexibility — for example, tracking user interactions via the pixel while recording page-related data via the server.

Key Features:

- Data is collected by both the tracking pixel and the Java library.
- No custom track domain is required.
- Pixel requests are forwarded to the server library, which sends them via the FILE consumer to the track server. `MappIntelligenceHybrid` always switches the consumer type to FILE unless it is set to CUSTOM; the log files are then sent by the [cron job](cron-job.md).

#### Configuration Example:

```java
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.FILE);
```

---

### 3. Tracking Mission-Critical Information

Use the Java library exclusively for mission-critical data (for example, orders, confirmations, or shipping events), while the tracking pixel continues to collect less sensitive data.

Key Features:

- Reduces load on the customer’s server.
- The pixel sends regular browser data directly to the track server.
- The Java library uses the HTTP_CLIENT consumer to send critical data immediately.

#### Configuration Example:

```java
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.HTTP_CLIENT)
    .setMaxAttempt(3)
    .setAttemptTimeout(100)
    .setMaxBatchSize(1);
```

---

### 4. Server Library as Track Domain

All requests are collected through the tracking pixel but routed via your server to Mapp Intelligence. This approach hides the Mapp Track Domain from the browser and allows full client-side functionality while retaining server control.

> [!TIP]
> **Recommendation**
>
> Use the [MappIntelligenceHybrid](classes.md) class with [Cronjob](cron-job.md) logic for this use case.

---

## Lifecycle and Threading

`MappIntelligenceTracking` and `MappIntelligenceHybrid` carry the data of the current request (user agent, IP address, cookies, everID) and are **not thread-safe**. Create one instance per request and close it when the request is done. If a second thread tracks through the same instance, the library logs a warning once, because the data of different users would be mixed. The warning has the level WARN, so it is not visible with the default log level `ERROR`.

Both classes are `AutoCloseable`, so the recommended pattern is try-with-resources:

```java
try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(config)) {
    tracking.track(dataMap);
}
```

- `close()` sends the requests that are still in the queue, waits up to 30 seconds (one deadline for all queues, including retries) for them to be handed to the consumer, and then releases the thread pools and, for a FILE consumer created by the library, the file handle. It is idempotent - calling it a second time does nothing. `MappIntelligence` itself extends `AutoCloseable`, so try-with-resources also works with a variable of the interface type.
- `flush()` is best effort: it only hands the queued requests to the background threads and returns immediately. If a flush for a queue is already pending, the call is merged into it.
- An instance that is never closed sends its remaining requests and releases its resources only once it has been garbage collected or the JVM terminates. There is no guarantee when that happens, so always call `close()`.
- All background threads are daemon threads named `mapp-intelligence-*`, so they never keep the JVM alive.
- With the FILE consumer, all instances of one JVM that use the same `filePath` and `filePrefix` share one temporary file, so one instance per request is safe. Several JVMs or processes may write to the same `filePath` and `filePrefix` as well: they and the cron job are coordinated with an exclusive lock on `<filePath>/<filePrefix>.lock` (see [Data Transfer: Consumer](data-transfer-consumer.md)).
- A consumer passed in with `setConsumer` belongs to you: it is not closed by the library and may be shared by several instances (see [Data Transfer: Consumer](data-transfer-consumer.md)).
