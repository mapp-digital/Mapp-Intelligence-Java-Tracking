# Mapp Intelligence - Server-side Java tracking library

[Site](https://mapp.com/) |
[Docs](https://documentation.mapp.com/latest/en/java-library-7239408.html) |
[Support](https://support.webtrekk.com/) |
[Changelog](https://documentation.mapp.com/latest/en/java-changelog-10537432.html)

Server-side tracking is crucial for companies needing to measure mission-critical information on their website, such 
as order information or other website goals.

The Java library of Mapp Intelligence helps customers to setup server-side tracking when using Mapp Intelligence as 
their analysis tool.

The basis for the data collection on your server is implementing the respective library. The Mapp Intelligence tracking 
library provides scripts to track user behavior and technical information, such as user agents, among others. In 
addition to the standard data collection, the tracking library offers many options to customize tracking based on 
specific use cases. Typical use cases are product, order and shipment tracking or the tracking of application processes.

# Usage notes

## One instance per request, closed per request

`MappIntelligenceTracking` and `MappIntelligenceHybrid` carry the data of the current request (user agent, IP,
cookies, ever ID) and are **not thread-safe**. Create one instance per request and close it when the request is done.
Both are `AutoCloseable`; `close()` sends the remaining requests, waits for them to be handed to the consumer (up to
30 seconds) and releases the thread pools and - for a consumer the library created itself - the file handle (a consumer
passed in with `setConsumer` is not closed):

```java
try (MappIntelligenceTracking tracking = new MappIntelligenceTracking(config)) {
    tracking.track(dataMap);
}
```

`flush()` only hands the requests to the background threads and returns immediately. An instance that is never
closed sends its remaining requests only once it has been garbage collected or the JVM terminates - there is no
guarantee when that happens.

## Campaigns once per session

With `MappIntelligenceCampaign.setOncePerSession(true)` a campaign is transmitted only once per session. The library
remembers the transmitted campaigns in the `wtstp_tcc` cookie, which you have to add to your HTTP response yourself -
before the response is committed:

```java
MappIntelligenceCookie campaigns = tracking.getTransmittedCampaignsCookie();
if (campaigns != null) {
    Cookie cookie = new Cookie(campaigns.getName(), campaigns.getValue());
    cookie.setPath(campaigns.getPath());
    cookie.setSecure(campaigns.isSecure());
    cookie.setHttpOnly(campaigns.isHttpOnly());
    cookie.setMaxAge(campaigns.getMaxAge());
    response.addCookie(cookie);
}
```

Without an explicit campaign ID, the ID is read from the media code parameters of the request URL (`mc`, `wt_mc`
by default, see `setMediaCode`).

## Custom consumers

A custom `MappIntelligenceConsumer` only has to implement `sendBatch()`: `true` means delivered, `false` means the
batch goes back into the queue and is retried. To drop a batch that can never be sent (for example a permanent client
error) instead of retrying it forever, override `send()` and return `BatchResult.DISCARDED`. A consumer passed in
through the configuration belongs to you and is not closed by the library.

# Development

## Requirements

| Software         | Version     |
|------------------|------------:|
| `docker`         |     `19.0+` |
| `docker-compose` |     `1.24+` |
| `make`           |             |

## Build

Build test and compile *Mapp Intelligence - Server-side Java tracking library* with Java 8 inside a docker container. The jars `mapp-intelligence-java-tracking.jar` and `mapp-intelligence-java-cronjob.jar` are also copied to `dist/`.

```bash
$ make build
```

## Test

Test *Mapp Intelligence - Server-side Java tracking library* with Java 8, 11, 17 and 18 inside a docker container.

```bash
$ make test-all
```

Test *Mapp Intelligence - Server-side Java tracking library* with the current latest Java version (Java 25) inside a docker container.

```bash
$ make test-latest
```

Test *Mapp Intelligence - Server-side Java tracking library* with a single Java version inside a docker container (image `eclipse-temurin:<n>-jdk-jammy`). The shortcuts `make test-java<n>` exist for Java 8, 11, 17, 18, 19, 20, 21, 22 and 25.

```bash
$ make test JAVA_VERSION=21
$ make test-java21
```

Stress test of the file consumer and the cron job with Java 8: several JVMs write into the same request logging files while the cron job takes them over. `STRESS_ARGS` sets the size of the test.

```bash
$ make stress-test
$ make stress-test STRESS_ARGS="-Dstress.jvms=8 -Dstress.threads=4 -Dstress.requests=5000 -Dstress.maxFileLines=100"
```

## Demo

Start a demo shop example on https://localhost:8443.

```bash
$ make demo
```