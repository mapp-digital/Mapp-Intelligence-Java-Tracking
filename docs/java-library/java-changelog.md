# Changelog

> Based on the public documentation <https://docs.mapp.com/docs/java-changelog> (retrieved 2026-09-29), updated to the current code of this repository.

## 1.1.0 (unreleased)

### Upgrade notes

Existing code keeps working without changes. The following changes in behavior are worth knowing before upgrading:

- **Close tracking instances (recommended):** `MappIntelligenceTracking` and `MappIntelligenceHybrid` are `AutoCloseable`. `close()` sends the remaining requests, waits up to 30 seconds for them to be handed to the consumer and releases thread pools and file handles. Create one instance per request and close it, ideally with try-with-resources. An instance that is never closed still sends its remaining requests, after it has been garbage collected or at the latest when the JVM terminates. (see [Classes](classes.md))
- **Permanent HTTP errors are no longer retried:** The HTTP consumer retries server errors (5xx) as well as 408 and 429. Every other client error (4xx) discards the batch with an error log entry, instead of blocking the queue forever. A batch that exceeds the hard limits (10,000 requests or 24 MB) is discarded as well. Note that requests rejected because of a misconfiguration (e.g. a wrong track ID or track domain) are therefore no longer kept until the configuration is fixed. (see [Data Transfer: Consumer](data-transfer-consumer.md))
- **Cron job:** The requests of a log file that can never be sent are kept in `*.log.discarded` (only these, not the delivered ones; a later run appends to it) and no longer picked up, and the run continues with the next file instead of aborting. The data stays on disk, and the run still ends with a failure exit status. If the same file also has requests that are left to retry, it is kept as `.log` with only those. No log files to send is not an error any more (exit status 0), and `--help` / `--version` end the script with exit status 0 without sending. A run that starts while another run is still sending the files of the same file path and prefix ends right away with exit status 0 without sending anything - monitoring that expects every run to send sees no error. The cron job creates the lock file `<prefix>.cronjob.lock` in the file path (it stays there), and it rewrites a `.log` file that was sent partially via a helper file `<file>.log.<random>.part`, so it needs write access to the directory. (see [Cron Job](cron-job.md))
- **`maxQueueSize` takes effect:** When a queue reaches the maximum queue size, it is sent; if that flush fails, the oldest requests are discarded (logged at level WARN). `0` means unlimited, the cron job always uses an unlimited queue. (see [Integration and Configuration](integration-and-configuration.md))
- **File consumer:** Temporary files are named `<prefix>-<timestamp>.<random>.tmp`, and a lock file `<prefix>.lock` is created next to them. Several JVMs and the cron job are coordinated with it (see Bugfixes).
- **File limits:** Too large values for `maxFileLines`, `maxFileDuration` and `maxFileSize` are reduced to the maximum with a warning instead of being ignored silently.
- **Always SSL:** Every request is sent via `https://`. `setForceSSL()` and the config file key `consumer.forceSSL` are deprecated and ignored; `setForceSSL(false)` no longer switches to `http://`. (see [Integration and Configuration](integration-and-configuration.md))
- **Campaigns without an ID:** Because `setMediaCode()` now takes effect (default media codes `mc` and `wt_mc`), a campaign object without an explicit ID reads the campaign from the request URL. Where nothing was sent before, a campaign is tracked now.
- **Cron job and file consumer prefix:** Only files named `<filePrefix>-<timestamp>…` are picked up, so the prefix `Shop` no longer takes the files of the prefixes `ShopDE` and `Shop-DE` (before, the file consumer even continued the temporary file of `Shop-DE`). The files written by the library always have this form.
- **Hybrid:** Only pixel requests with the parameter `p` and at most 64 KB are written into the log files, any other request is ignored with a warning. The parameters that only the server adds (`X-WT-IP`, `X-WT-UA`, `X-WT-SEC-CH-UA*`) are removed from the request of the client; the library adds them from the configuration, if they are set there. `track(url)` with an invalid URL tracks nothing (before: the previous request again). (see [Classes](classes.md))
- **Page URL (`pu`):** contains the protocol of the request URL (`http://` or `https://`) instead of always `https://`.

#### Only relevant for subclasses of built-in classes and own implementations

- `close()` of `MappIntelligenceConsumerFile`, `MappIntelligenceConsumerHttpClient` and `MappIntelligenceHybrid` is `public` now. A subclass that overrides it as `protected` no longer compiles and has to declare it `public`.
- The built-in consumers no longer extend `AbstractMappIntelligenceCleaner` and therefore no longer implement `Runnable`. Code that calls `run()` on a consumer or passes it as a `Runnable` has to call `close()` instead.
- The interface `MappIntelligence` extends `AutoCloseable` and declares `void close()`. An own implementation of the interface has to implement `close()`.

### Features

- **Campaigns:** `setMediaCode()` and `setOncePerSession()` take effect server-side. Without an explicit ID the campaign is read from the media code parameters of the request URL, and with once per session an already transmitted campaign is not sent again. The transmitted campaigns are kept in the cookie `wtstp_tcc`, available via `getTransmittedCampaignsCookie()`. (see [Campaign](data-objects-mapp-intelligence-server2server-java/campaign-data-object-java-library-s2s.md))
- **Engage registration:** New fields ISO code, ISO language and time zone. (see [Mapp Engage](data-objects-mapp-intelligence-server2server-java/mapp-engage-data-object-java-library-s2s.md))
- **Engage attributes:** Every value type is accepted (e.g. `Boolean`, `Long`, `Double`, `BigDecimal`), not only `String` and `Integer`.
- **Interface `MappIntelligence`:** extends `AutoCloseable`, so code typed against the interface can use try-with-resources as well. `close()` declares no checked exception. (see [Interfaces](interfaces.md))
- **Cron job:** Expired temporary files are detected with the configured `maxFileDuration` instead of a fixed 30 minutes. A log file that was sent completely but cannot be deleted is renamed to `*.log.sent` instead of being sent again on the next run. Only one run at a time sends the log files of a file path and prefix (exclusive lock on `<prefix>.cronjob.lock`, released by the operating system when the process ends). A log file that was sent partially is rewritten with only the requests that were not delivered, in the order of the file. (see [Cron Job](cron-job.md))
- **Custom consumers:** New default method `MappIntelligenceConsumer.send()` returning `BatchResult` (`SENT`, `RETRY`, `DISCARDED`), so a consumer can drop a batch that can never be sent. Existing consumers that only implement `sendBatch()` keep working. (see [Interfaces](interfaces.md))
- **HTTP consumer:** Connections are reused. The response is read completely and the connection is handed back to the keep-alive cache of the JDK instead of being closed, so consecutive batches no longer need a new TCP connection and a new TLS handshake each. How long an idle connection stays open is determined by the keep-alive timeout of the server. A connection with an I/O error is discarded. (see [Data Transfer: Consumer](data-transfer-consumer.md))

### Bugfixes

- Product collections: parameters and categories that only some of the products have are no longer dropped.
- Engage attributes are JSON-escaped; a quote in a value no longer breaks the `eaj` parameter.
- A user agent, cookie or client hint containing an invalid percent-encoding (e.g. a bare `%`) no longer throws.
- Query parameters whose value contains `=` are no longer dropped; a query parameter without a value, or a referrer without a protocol, no longer throws.
- Resource leaks: only one JVM shutdown hook for all instances, thread pools are shut down, all threads are daemon threads, and the file writer is closed.
- File consumer: thread-safe with several request queues, and a write error keeps the batch in the queue instead of reporting it as sent.
- File consumer: several JVMs and the cron job writing into the same file path and prefix lost requests (up to about half of them with several JVMs): a temporary file rotated by another process was created again with the same name, and its later rename silently overwrote the existing log file. Every write, rotation and takeover now happens under an exclusive file lock (`<prefix>.lock`), the identity of the open file is checked before every write, a found file is never created again, temporary file names are unique and a rename never replaces an existing file. `make stress-test` verifies this with several JVMs and a concurrent cron job.
- File consumer: a `filePath` without a trailing path separator wrote the files into the parent directory, where they were never found again nor sent.
- File consumer: a `maxBatchSize` that is overwritten with 1 is logged at level DEBUG.
- Cron job: a log file is deleted only after every request queue has reported success, and is read completely.
- Adding requests to a queue no longer slows down with the queue size (large cron job files).
- The 20 MB limit per batch is calculated exactly. The batch size was summed up as a `float`, which is rounded above about 16.7 million characters, so a batch could be cut at the wrong request.
- File consumer: all instances of one JVM that write to the same file path and file prefix share one temporary file. With one tracking instance per request, several instances no longer write into the same file with separate line counters, and a rotation no longer renames the file while other instances are still writing into it. A temporary file that was renamed or deleted from outside (e.g. by the cron job) is recreated before the next write.
- `null` passed to a setter of a data object counts as not set and no longer throws a `NullPointerException` (e.g. `setName(null)`, `setParameter(1, null)`, `MappIntelligenceProduct.setStatus(null)`, `MappIntelligenceCampaign.setId(null)`, `MappIntelligenceEngage.setAttributes(null)`, `MappIntelligenceProductCollection.add(null)`, `MappIntelligenceParameterMap.add(key, null)`).
- Configuration: `setLogLevel((String) null)` and `new MappIntelligenceConfig((String) null)` no longer throw; an integer larger than `Integer.MAX_VALUE` in a config file falls back to the default instead of throwing.
- Configuration: list values from a config file are trimmed and empty entries are dropped (an empty `containsInclude` entry matched every URL). The lists are modifiable, so the `add...()` methods work after loading a config file.
- Configuration: the list setters store a copy of the passed list, so an immutable list (e.g. `Collections.emptyList()`) no longer breaks `build()` or the `add...()` methods.
- Configuration: a reused configuration derives the own domain from the current request URL instead of keeping the host of the first request, and re-activates tracking once all include/exclude filters are removed.
- HTTP consumer: with `setOnSessionId()` and plain HTTP, the callback threw a `ClassCastException` after the batch had been sent, so the batch was retried and sent twice. All requests are sent via SSL now, and the callback is skipped for any other connection type.
- HTTP consumer: `setOnSessionId()` reports the TLS session of the connection the batch was actually sent over. Before, a second SSL connection was opened to port 443 for every batch only to read its session ID.
- `getUserIdCookie(null, null)` no longer throws: a `null` context counts as client side, a `null` pixel version like an unknown one.
- Cron job: a successful rename of an expired temporary file is logged at level INFO instead of ERROR.
- Cron job: a log file that could not be read counted as empty and was deleted, although nothing was sent.
- `track(MappIntelligenceParameterMap)` changed the map of the caller: a reused parameter map carried the IP address, user agent and advanced permission of the previous request into the next one and lost the page name. The queue works on a copy as well.
- A `//` in the path or a URL in a query value of the request URL cut the default page name and the page URL (`pu`).
- The query parameters of the request URL were decoded before they were split: an encoded `&` or `=` cut a value (`useParamsForDefaultPageName`, media codes of campaigns).
- The presence of `X-WT-IP`, `X-WT-UA` and the client hints in a request string is checked by parameter name: a value that contains the name (e.g. `cs1=see X-WT-UA`) no longer suppresses them.
- Hybrid: a client could set `X-WT-IP` / `X-WT-UA` and so pretend to be another user.
- `setTemporarySessionId("")` no longer sends the type `fpt` alone.
- Cron job: two overlapping runs sent the same log files twice; a log file that was sent partially was sent completely again with the next run.
- Cron job: a `requestInterval` of the configuration (`new MappIntelligenceCronjob(config)`) could flush in the middle of a log file; the cron job sends without interval now.

---

## 1.0.4 - 1.0.8

Release and publishing changes only, no functional changes.

---

## 1.0.3 (2025-10-17)

### Breaking changes

This version introduces a new background mechanism that collects requests and sends them in batches at regular intervals. This improves processing efficiency and ensures stable performance, even as traffic increases, by distributing requests across multiple queues.

#### Key changes in detail

- **Batch request processing:** Requests are no longer sent immediately when a `track()` call is triggered. Instead, they are collected in internal queues and sent at defined intervals.
- **Load distribution across multiple queues:** Requests are now processed through multiple queues, helping to evenly distribute the load and maintain stability under heavy traffic.
- **Manual or automatic flushing:** Requests are automatically sent at specified intervals or can be manually triggered, giving you more control over when data is transmitted.

#### Developer Impact

If your application previously relied on requests being sent immediately after an action, this behavior may no longer work as expected. You need to either configure an interval for automatic flushing or implement manual triggers to send requests at the right time. (see [Classes](classes.md)).

### Features

- **PII support for Engage registration:** Personally identifiable information can now be securely transmitted during the registration process. (see [Mapp Engage](data-objects-mapp-intelligence-server2server-java/mapp-engage-data-object-java-library-s2s.md))
- **Custom Attributes for Engage:** It is now possible to send Mapp Engage Custom Attributes directly via Server-to-Server tracking. (see [Mapp Engage](data-objects-mapp-intelligence-server2server-java/mapp-engage-data-object-java-library-s2s.md))
- **Flexible data forwarding:** You can now choose whether data should be forwarded only to Mapp Intelligence or to both Mapp Intelligence and Mapp Engage. (see [Integration and Configuration](integration-and-configuration.md))

---

## 0.1.5 (2023-08-07)

### Feature

- Added method for configuring connection and read timeout (see [Integration and Configuration](integration-and-configuration.md))

### Bugfix

- At connection timeout the request is written back to the queue

---

## 0.1.3 (2023-08-02)

### Feature

- Support temporary session ID (see [Data](https://docs.mapp.com/docs/data))

---

## 0.1.0 (2023-05-08)

### Feature

- Include advanced logging (see [Integration and Configuration](integration-and-configuration.md))
- Support UserAgent Client Hints (see [Integration and Configuration](integration-and-configuration.md))

### Bugfix

- Update outdated dependencies

---

## 0.0.22 (2023-02-28)

### Bugfix

- Add artifact *com.mapp.intelligence.tracking:mapp-intelligence-java-tracking:pom* to central

---

## 0.0.17 (2023-01-24)

### Feature

- Include method to suppress certain requests (see [Integration and Configuration](integration-and-configuration.md)).

---

## 0.0.16 (2022-10-24)

### Optimization

- Add tracking parameter (pf, cs801 and cs802) for tracking feature usage.

---

## 0.0.1 (2020-11-03)

- Beta release
