# Data Transfer: Consumer

> Based on the public documentation <https://docs.mapp.com/docs/data-transfer-consumer> (retrieved 2026-09-29), updated to the current code of this repository.

Consumers are used to send the track requests to the Intelligence track server. There are ...

- **HTTP_CLIENT**: Saves track requests in memory and sends them to the server using separate parallel threads.
- **FILE**: Requests are saved in a separate file and send via a cronjob to the track server in a specified interval.
- **CUSTOM**: Create your own consumer to send the tracking requests.

All consumers are fed by the same request queues: tracked requests are distributed over `requestQueues + 1` in-memory queues, and each queue is processed by its own background thread (a daemon thread named `mapp-intelligence-queue-<n>`). Requests with the same everID always end up in the same queue; requests without an everID are distributed by user agent and IP address, and the last queue is a fallback for requests that have neither. The queues are sent when `flush()` is called, every `requestInterval` seconds if an interval is configured, and when the tracking instance is closed (see [Lifecycle and Threading](README.md#lifecycle-and-threading)).

What happens with a batch is decided by the consumer:

| Result | Meaning |
|---|---|
| `SENT` | The batch was delivered and leaves the queue. |
| `RETRY` | The batch could not be delivered right now. It goes back to the front of its queue and is retried after `attemptTimeout` milliseconds, up to `maxAttempt` attempts per flush. After the last attempt the requests **stay in the queue** and are sent again with the next flush (or when the instance is closed). |
| `DISCARDED` | The batch can never be sent (for example a permanent client error). It leaves the queue with an error log entry, so it cannot block the requests behind it. |

> [!NOTE]
> Requests that are still in the memory queue when the tracking instance is closed get one last flush with up to `maxAttempt` attempts, within the 30-second deadline of `close()`. Whatever is still undelivered after that is lost. Each queue is bounded by `maxQueueSize`: when a queue reaches it, the queue is sent; if that flush fails, the oldest requests are discarded (logged at level WARN) until the queue is back at `maxQueueSize`. `0` means unlimited.

**HTTP_CLIENT**

The ***HTTP_CLIENT*** consumer is ideal for low and high-traffic websites or when tracking specific pages. It ensures fast response times and efficient processing of tracking requests.

With newly introduced mechanisms, requests can now be collected in the background and sent out in batches at regular intervals. The system also distributes processing evenly across multiple queues, enabling stable performance even as traffic increases.

For websites with consistently high traffic, the ***FILE*** consumer remains the recommended option for reliable and scalable handling.

When using the ***HTTP_CLIENT*** consumer and sending browser requests via the pixel to the track server directly, it is important to set the max batch size to 1 to avoid processing issues and false analyses in Intelligence.

By default, the attempt timeout is set to 100 milliseconds, with a maximum batch size of 50 requests, one attempt per flush (`maxAttempt` 1), two request queues plus the fallback queue (`requestQueues` 2) and no automatic interval (`requestInterval` 0). The connection timeout defaults to 30,000 ms and the read timeout to 5,000 ms. The maximum queue size defaults to 1000 requests per queue (see the note above).

The HTTP_CLIENT consumer sends each batch as one `POST` request to `https://<trackDomain>/<trackId>/batch`, using the configured `connectionTimeout` and `readTimeout`. The response decides what happens with the batch:

| Response | Result |
|---|---|
| Any status below 400 | `SENT` |
| 5xx, 408 (Request Timeout), 429 (Too Many Requests) | `RETRY` |
| Any other 4xx (e.g. 400, 404, 413) | `DISCARDED`, logged at error level |
| Network error (connection refused, timeout, ...) | `RETRY` |
| Batch with more than 10,000 requests or a payload of 24 MB or more | `DISCARDED`, logged at error level |
| Empty payload | `RETRY` |

> [!NOTE]
> A permanent client error (for example caused by a wrong track ID) discards the affected batches instead of retrying them forever. Watch the error log when setting up a new configuration. The library itself closes a batch once it reaches about 20 MB; keep `maxBatchSize` at 10,000 or below, otherwise a batch with more than 10,000 requests is discarded.
>
> Both limits (24 MB and 20 MB, with 1 MB = 1024 × 1024) are measured as `String.length()` of the payload, i.e. in UTF-16 characters of the requests joined with the platform line separator, not in bytes.

```java
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.HTTP_CLIENT)
    .setMaxAttempt(1)
    .setAttemptTimeout(100)
    .setMaxBatchSize(1)
    .setMaxQueueSize(1000)
    .setRequestInterval(0)
    .setRequestQueues(2);
```

**FILE**

Using a ***FILE*** transfer for high traffic websites because it helps to reduce load and memory used by your server. Instead of keeping the requests in memory until they are sent to the track server, it writes them to a log file, and the [cron job](cron-job.md) sends the log files in batches to the track server. This solution is also more reliable in data collection: if sending a log file fails, the cron job keeps the requests that were not delivered in the log file and sends them in its next run, while the ***HTTP_CLIENT*** consumer only keeps undelivered requests in memory.

How the FILE consumer works:

- Requests are appended to a temporary file `<filePath>/<filePrefix>-<timestamp>.<random>.tmp`. The random part keeps the names unique, so a file created in the same millisecond in which another one was rotated cannot overwrite its log file later. `filePath` is a directory, with or without a trailing path separator.
- Before a batch is written, the file is checked against `maxFileLines`, `maxFileDuration` (in milliseconds) and `maxFileSize` (in bytes). A limit is reached if the lines already in the file plus the lines of the batch exceed `maxFileLines`, if the file is older than `maxFileDuration`, or if the file is already larger than `maxFileSize` (strictly greater). Then the writer is closed, the file is renamed to `.log` and a new `.tmp` file is started. If the rename fails, `Create new file, because cannot rename temporary file` is logged at error level and a new `.tmp` file is started anyway. The cron job only sends `.log` files; it also renames `.tmp` files that are older than its own `maxFileDuration` (default 30 minutes), see [Cron Job](cron-job.md).
- On start the consumer reuses an existing `<filePrefix>-<timestamp>….tmp` file in the file path (its line count is read from the file, its age from the timestamp in the file name). A file that was found is opened without creating it: if it was rotated in the meantime, the next one is tried or a new file is created exclusively (`CREATE_NEW`). Before every write it checks, under the file lock, that the open file is still the `.tmp` file (by its file key); if it was renamed or deleted from outside (for example by the cron job), it reopens another existing `.tmp` file or creates a new one.
- For the FILE consumer the library always sets the maximum batch size to 1 (every request is written as its own batch); a configured `maxBatchSize` is ignored.
- All `MappIntelligenceConsumerFile` instances in one JVM with the same `filePath` and `filePrefix` share **one** temporary file, one writer and one line counter; writing and file rotation are synchronized on this shared file. So one tracking or hybrid instance per request (each with its own consumer) is safe, and the files are still rotated correctly. The shared file is reference counted and closed when the last consumer using it is closed.
- Several JVMs or processes writing to the same `filePath` and `filePrefix`, and the cron job, are coordinated with an exclusive lock on the lock file `<filePath>/<filePrefix>.lock` (`FileChannel.lock()`): every write, rotation and takeover of an expired `.tmp` file by the cron job happens under this lock, and a rename never replaces an existing file. So no request is overwritten or lost. `make stress-test` in the repository verifies this with several JVMs and a concurrently running cron job (`STRESS_ARGS` for the size). Only files named `<filePrefix>-<timestamp>…` (the prefix, a `-` and then the digits of the timestamp) belong to a prefix, both for the file consumer and for the cron job, so the prefix `Shop` takes neither the files of `ShopDE` nor those of `Shop-DE`.
- If writing fails (for example an `IOException`) or no file could be opened, the result is `RETRY`: the batch stays in the queue instead of being lost. An empty payload is `RETRY` as well. A single request of 24 MB or more is `DISCARDED`, like with the HTTP_CLIENT consumer.
- A consumer created by the library is closed when the tracking instance is closed; the file itself is closed once the last consumer for the same path and prefix is closed.

> [!TIP]
> Recommendation
>
> We recommend using a [cron job](cron-job.md) to send the requests to the server every few minutes and empty your file to keep it at a manageable size.

```java
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.FILE)
    .setFilePath("/path/to/directory/")
    .setFilePrefix("MappIntelligenceRequests")
    .setMaxAttempt(1)
    .setAttemptTimeout(100)
    .setMaxBatchSize(50) // ignored for the FILE consumer, always 1
    .setMaxFileLines(5000)
    .setMaxFileDuration(30 * 60 * 1000)
    .setMaxFileSize(12 * 1024 * 1024);
```

**CUSTOM**

Using a ***CUSTOM*** consumer to create your own consumer to send the tracking requests. For more information about ***CUSTOM*** consumers, see [MappIntelligenceConsumer](interfaces.md).

A custom consumer only has to implement `boolean sendBatch(List<String> batchContent)`: `true` means the batch was delivered (`SENT`), `false` means it goes back into the queue and is retried (`RETRY`).

```java
class TestCustomConsumer implements MappIntelligenceConsumer {
    public boolean sendBatch(List<String> batchContent) {
        // implement custom consumer logic
        return true;
    }
}
MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
    .setConsumer(new TestCustomConsumer());
```

To drop a batch that can never be sent instead of retrying it forever, additionally override the default method `send(List<String>)` and return `MappIntelligenceConsumer.BatchResult.DISCARDED`. The library calls `send()`; its default implementation delegates to `sendBatch()`.

```java
class TestCustomConsumer implements MappIntelligenceConsumer {
    public boolean sendBatch(List<String> batchContent) {
        return this.send(batchContent) == BatchResult.SENT;
    }

    @Override
    public BatchResult send(List<String> batchContent) {
        // implement custom consumer logic
        // return BatchResult.SENT, BatchResult.RETRY or BatchResult.DISCARDED
        return BatchResult.SENT;
    }
}
```

Things to keep in mind for custom consumers:

- If `send()` or `sendBatch()` throws a `RuntimeException` or `send()` returns `null`, the batch is treated as `RETRY`; the exception is logged at error level.
- The consumer is called from all queue threads of an instance at the same time and may be shared by several instances, so it must be thread-safe.
- A consumer passed in with `setConsumer` belongs to you and is **not** closed by the library, not even when the tracking instance is closed.
- A consumer set with `setConsumer` is used for sending regardless of the consumer type. Set the type to `CUSTOM` anyway: `MappIntelligenceHybrid` switches every other type to FILE, and for the FILE type the batch size is forced to 1.
- With the type `CUSTOM` (or any unknown type, `setConsumerType` does not validate the string) but without `setConsumer`, no consumer exists: every flush logs `Cannot send because no consumer is available or an undefined consumer type was specified.` at error level, and the requests stay in the queue forever.
- Extending a built-in consumer (`MappIntelligenceConsumerHttpClient`, `MappIntelligenceConsumerFile`): their `sendBatch()` returns `true` for a discarded batch as well (it must leave the queue); use `send()` to tell the two apart. A subclass that only overrides `sendBatch()` keeps that override in effect, because the built-in `send()` then calls it.

### Example: Kafka

Writes every request as one message into a Kafka topic (client library `org.apache.kafka:kafka-clients`). `KafkaProducer` is thread-safe, so one producer can be shared by all queue threads and tracking instances. The batch counts as `SENT` only when the broker has confirmed every message; if a message fails, the whole batch is `RETRY`.

```java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

class KafkaTrackingConsumer implements MappIntelligenceConsumer {
    private final Producer<String, String> producer;
    private final String topic;

    KafkaTrackingConsumer(Producer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    public boolean sendBatch(List<String> batchContent) {
        List<Future<RecordMetadata>> results = new ArrayList<>(batchContent.size());
        for (String request : batchContent) {
            results.add(this.producer.send(new ProducerRecord<>(this.topic, request)));
        }

        try {
            for (Future<RecordMetadata> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
            return true;
        } catch (ExecutionException | TimeoutException e) {
            // broker not reachable: the batch goes back into the queue
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.ACKS_CONFIG, "all");
// one producer per application, close it yourself when the application stops
Producer<String, String> producer = new KafkaProducer<>(props);

MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
    .setConsumer(new KafkaTrackingConsumer(producer, "mapp-intelligence-requests"));
```

A `KafkaException` thrown by `send()` (for example when the producer buffer is full) is a `RuntimeException`, so the library logs it and treats the batch as `RETRY`.

### Example: Redis

Appends the requests to a Redis list (client library `redis.clients:jedis`). `JedisPool` is thread-safe; every call takes its own connection from the pool. `RPUSH` with all requests of the batch is one command, so the batch is appended completely or not at all.

```java
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

class RedisTrackingConsumer implements MappIntelligenceConsumer {
    private final JedisPool pool;
    private final String key;

    RedisTrackingConsumer(JedisPool pool, String key) {
        this.pool = pool;
        this.key = key;
    }

    public boolean sendBatch(List<String> batchContent) {
        try (Jedis jedis = this.pool.getResource()) {
            jedis.rpush(this.key, batchContent.toArray(new String[0]));
            return true;
        } catch (JedisException e) {
            // Redis not reachable: the batch goes back into the queue
            return false;
        }
    }
}

// one pool per application, close it yourself when the application stops
JedisPool pool = new JedisPool("redis", 6379);

MappIntelligenceConfig config = new MappIntelligenceConfig()
    .setTrackDomain("analytics01.wt-eu02.net")
    .setTrackId("111111111111111")
    .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
    .setConsumer(new RedisTrackingConsumer(pool, "mapp-intelligence-requests"));
```

### Forwarding the requests from Kafka or Redis

Kafka and Redis only buffer the requests. A separate process of your own (a worker) has to read them and send them to the track server, the same way the HTTP_CLIENT consumer does:

- `POST` to `https://<trackDomain>/<trackId>/batch` with the content type `text/plain`, one request per line in the body.
- At most 10,000 requests and less than 24 MB per batch.
- Handle the response like the HTTP_CLIENT consumer (see the table above): a status below 400 means delivered; 5xx, 408, 429 and network errors are retried; any other 4xx can never be delivered.
- Commit the Kafka offset, or remove the requests from the Redis list, only after the batch was delivered. With Redis and one worker per list, for example read with `LRANGE <key> 0 9999` and after delivery remove the batch with `LTRIM <key> <number of requests> -1`.

> [!NOTE]
> If a batch is retried after part of it already reached Kafka or Redis (for example a timeout after the broker has stored the message), these requests are buffered twice. Kafka and Redis do not prevent this, and neither does the worker. If duplicates matter, deduplicate in the worker.

### Example: Test consumer

Keeps the requests in memory instead of sending them, so you can check in your own unit tests what the library would track, without network or files. `setAvailable(false)` simulates an unreachable track server: every batch is then `RETRY` and stays in the queue.

```java
class InMemoryTestConsumer implements MappIntelligenceConsumer {
    // thread-safe, the consumer is called from all queue threads
    private final List<String> requests = new CopyOnWriteArrayList<>();
    private volatile boolean available = true;

    public boolean sendBatch(List<String> batchContent) {
        if (!this.available) {
            return false;
        }

        this.requests.addAll(batchContent);
        return true;
    }

    List<String> getRequests() {
        return new ArrayList<>(this.requests);
    }

    void setAvailable(boolean available) {
        this.available = available;
    }

    void clear() {
        this.requests.clear();
    }
}
```

Usage in a JUnit test. `flush()` only hands the requests to the background threads, so close the instance before you check the requests: `close()` waits until the remaining requests have been handed to the consumer.

```java
@Test
public void testTrackPage() {
    InMemoryTestConsumer consumer = new InMemoryTestConsumer();
    MappIntelligenceConfig config = new MappIntelligenceConfig()
        .setTrackDomain("analytics01.wt-eu02.net")
        .setTrackId("111111111111111")
        .setConsumerType(MappIntelligenceConsumerType.CUSTOM)
        .setConsumer(consumer);

    try (MappIntelligenceTracking mappIntelligence = new MappIntelligenceTracking(config)) {
        mappIntelligence.track(new MappIntelligenceDataMap().page(new MappIntelligencePage("en.home")));
    }

    assertEquals(1, consumer.getRequests().size());
    assertTrue(consumer.getRequests().get(0).contains("en.home"));
}
```

Please check [Configuration](integration-and-configuration.md) to see all configuration options and explanations of the Mapp Intelligence Java library, including default settings.
