# Interfaces

> Based on the public documentation <https://docs.mapp.com/docs/interfaces> (retrieved 2026-09-29), updated to the current code of this repository.

This section includes the available interfaces and examples.

**MappIntelligence**

### Properties

| Name | Description |
|---|---|
| MappIntelligence.VERSION | Mapp Intelligence version. |
| MappIntelligence.V4 | Identifier for pixel v4. |
| MappIntelligence.V5 | Identifier for pixel v5. |
| MappIntelligence.SMART | Identifier for smart pixel. |
| MappIntelligence.CLIENT_SIDE_COOKIE | Identifier for 1st party cookies (`"1"`). |
| MappIntelligence.SERVER_SIDE_COOKIE | Identifier for 3rd party cookies (`"3"`). |

### Methods

#### getUserIdCookie

Implemented by `MappIntelligenceTracking` and `MappIntelligenceHybrid` (see [Classes](classes.md)).

```java
/**
 * @param pixelVersion Version ot the current pixel (v4, v5, smart)
 * @param context Cookie context (CLIENT_SIDE_COOKIE = "1", SERVER_SIDE_COOKIE = "3")
 *
 * @return MappIntelligenceCookie
 */
MappIntelligenceCookie getUserIdCookie(String pixelVersion, String context);
```

#### close

`MappIntelligence` extends `AutoCloseable` and declares `close()` without a checked exception, so an instance can be used with try-with-resources without a `catch` block, also through the interface type (see ***close*** in [Classes](classes.md)).

```java
/**
 * Sends the remaining requests and releases all resources.
 */
void close();
```

```java
try (MappIntelligence tracking = new MappIntelligenceTracking(config)) {
    // ...
}
```

**MappIntelligenceCookie**

The cookies returned by the library (`getUserIdCookie`, `getTransmittedCampaignsCookie`) implement this interface. Values the library does not set are empty: domain `""` (use `setDomain` on your servlet cookie only if `getDomain()` is not empty), path `""`, max age `0`, Secure and HttpOnly `false`. The cookies returned by the library always set path, max age, Secure and HttpOnly (see [Classes](classes.md)).

### Methods

#### setDomain

```java
/**
 * @param domain Specifies the domain within which this cookie should be presented.
 */
void setDomain(String domain);
```

#### getDomain

```java
/**
 * @return Gets the domain name of this cookie.
 */
String getDomain();
```

#### setMaxAge

```java
/**
 * Sets the maximum age in seconds for this cookie.
 *
 * @param expiry Sets the maximum age in seconds for this cookie.
 */
void setMaxAge(int expiry);
```

#### getMaxAge

```java
/**
 * Gets the maximum age in seconds of this cookie.
 *
 * @return Gets the maximum age in seconds of this cookie.
 */
int getMaxAge();
```

#### setPath

```java
/**
 * @param uri Specifies a path for the cookie to which the client should return the cookie.
 */
void setPath(String uri);
```

#### getPath

```java
/**
 * @return Returns the path on the server to which the browser returns this cookie.
 */
String getPath();
```

#### setSecure

```java
/**
 * @param flag Indicates to the browser whether the cookie should only be sent using a secure protocol, such as
 *             HTTPS or SSL.
 */
void setSecure(boolean flag);
```

#### isSecure

```java
/**
 * @return  Returns true if the browser is sending cookies only over a secure protocol, or false if the browser
 *          can send cookies using any protocol.
 */
boolean isSecure();
```

#### getName

```java
/**
 * @return Returns the name of the cookie.
 */
String getName();
```

#### getValue

```java
/**
 * @return Gets the current value of this cookie.
 */
String getValue();
```

#### setHttpOnly

```java
/**
 * @param isHttpOnly Marks or unmarks this cookie as HttpOnly.
 */
void setHttpOnly(boolean isHttpOnly);
```

#### isHttpOnly

```java
/**
 * @return Checks whether this cookie has been marked as HttpOnly.
 */
boolean isHttpOnly();
```

**MappIntelligenceLogger**

Create your own LoggerClass to debug our Mapp Intelligence Java library. Pass it in with `setLogger` (see [Configuration](integration-and-configuration.md)); without a logger, nothing is logged.

- The library only calls `log(String msg)`, with an already formatted message that starts with `dd-MMMM-yyyy hh:mm:ss.SSS LEVEL [Mapp Intelligence]: ` (e.g. `29-September-2026 02:15:30.123 ERROR [Mapp Intelligence]: ...`). `log(String format, Object... args)` is part of the interface but not called by the library.
- The messages are already filtered by the configured `logLevel` (default `ERROR`).
- The logger is called from background threads (the queue threads) as well, so it must be thread-safe.

### Methods

#### log

```java
/**
 * @param msg Debug message
 */
void log(String msg);
```

```java
/**
 * @param format String format
 * @param args Arguments
 */
void log(String format, Object... args);
```

#### Example

```java
class CustomLogger implements MappIntelligenceLogger {
	/**
	 * @param msg Debug message
	 */
	@Override
	public void log(String msg) {
		System.out.println(msg);
	}

	/**
	 * @param format String format
	 * @param args   Arguments
	 */
	@Override
	public void log(String format, Object... args) {
		System.out.println(String.format(format, args));
	}
}
```

**MappIntelligenceConsumer**

Create your own ConsumerClass to use for data transfer to Intelligence. You pass it in via `setConsumer` (see [Configuration](integration-and-configuration.md) and [Data Transfer: Consumer](data-transfer-consumer.md)).

### Properties

| Name | Description |
|---|---|
| MappIntelligenceConsumer.BatchResult.SENT | The batch was delivered. |
| MappIntelligenceConsumer.BatchResult.RETRY | The batch could not be delivered right now. It goes back into the queue and is retried. |
| MappIntelligenceConsumer.BatchResult.DISCARDED | The batch can never be sent, no matter how often it is retried (for example a permanent client error or an oversized payload). It leaves the queue so it cannot block the requests behind it, but it was not delivered either; the flush reports `false` (see `setOnFlushComplete` in [Classes](classes.md)). |

### Methods

#### sendBatch

Return `true` if the batch was delivered. With `false` the batch goes back into the queue and is retried (see `setMaxAttempt` and `setAttemptTimeout` in [Configuration](integration-and-configuration.md)).

```java
/**
 * @param batchContent List of tracking requests
 *
 * @return boolean
 */
boolean sendBatch(List<String> batchContent);
```

#### send

Sends a batch and reports what happened to it. This is the method the library calls. The default implementation delegates to ***sendBatch*** and maps `true` to `BatchResult.SENT` and `false` to `BatchResult.RETRY`, so a consumer that only implements ***sendBatch*** keeps working. Override it if you need to drop a batch that can never be sent instead of retrying it (`BatchResult.DISCARDED`).

```java
/**
 * @param batchContent List of tracking requests
 *
 * @return Result of this batch
 */
default BatchResult send(List<String> batchContent);
```

> [!NOTE]
> - A `RuntimeException` thrown by your consumer and a `null` result are treated as `BatchResult.RETRY`: the exception is logged and the batch is kept in the queue.
> - The built-in consumers (`MappIntelligenceConsumerHttpClient`, `MappIntelligenceConsumerFile`) return `true` from ***sendBatch*** for a discarded batch as well, because it has to leave the queue; their ***send*** tells the two apart. A subclass of a built-in consumer that only overrides ***sendBatch*** keeps that override in effect.
> - One consumer instance is shared by all request queue threads, so it has to be thread-safe. The result of ***send*** belongs to exactly this call.
> - A consumer passed in via the configuration belongs to you and is not closed by the library, not even when the tracking instance is closed. If your consumer implements `AutoCloseable`, closing it is up to you. This allows one consumer to be shared by several tracking instances (one per request).

#### Example

```java
class TestCustomPrintConsumer implements MappIntelligenceConsumer {
	/**
	 * @param batchContent List of tracking requests
	 *
	 * @return boolean
	 */
	public boolean sendBatch(List<String> batchContent) {
		for (String request : batchContent) {
			System.out.println(request);
		}

		return true;
	}
}
```

```java
class TestCustomDiscardingConsumer implements MappIntelligenceConsumer {
	/**
	 * @param batchContent List of tracking requests
	 *
	 * @return boolean
	 */
	public boolean sendBatch(List<String> batchContent) {
		return this.send(batchContent) == BatchResult.SENT;
	}

	/**
	 * @param batchContent List of tracking requests
	 *
	 * @return Result of this batch
	 */
	@Override
	public BatchResult send(List<String> batchContent) {
		int status = this.transmit(batchContent); // your own transfer (see below), returns an HTTP status

		if (status >= 200 && status < 300) {
			return BatchResult.SENT;
		}

		// a permanent client error never succeeds, retrying it would only block the queue
		if (status >= 400 && status < 500 && status != 408 && status != 429) {
			return BatchResult.DISCARDED;
		}

		return BatchResult.RETRY;
	}

	/**
	 * Your own transfer of the batch, e.g. with an HTTP client of your choice.
	 *
	 * @param batchContent List of tracking requests
	 *
	 * @return HTTP status of the response
	 */
	private int transmit(List<String> batchContent) {
		// implement your own transfer logic
		return 200;
	}
}
```
