package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceConsumerHttpClient extends AbstractMappIntelligenceConsumer {
    /**
     * HTTP 429, which HttpURLConnection has no constant for.
     */
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * Constant for default connection timeout.
     */
    private final int connectionTimeout;
    /**
     * Constant for default read timeout.
     */
    private final int readTimeout;

    /**
     * Set by the caller's thread, read by the queue executor threads - volatile so the callback is
     * visible to a flush that is already running from the requestInterval scheduler.
     */
    private volatile SessionTrackingSocketFactory sessionTracking = null;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceConsumerHttpClient(Map<String, Object> config) {
        super(config);

        this.connectionTimeout = (int) config.getOrDefault("connectionTimeout", DEFAULT_CONNECT_TIMEOUT);
        this.readTimeout = (int) config.getOrDefault("readTimeout", DEFAULT_READ_TIMEOUT);
    }

    private HttpURLConnection getHttpURLConnection(
        URL url,
        String payload,
        SessionTrackingSocketFactory tracking
    ) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain; utf-8");

        con.setConnectTimeout(this.connectionTimeout);
        con.setReadTimeout(this.readTimeout);

        // every request is sent via SSL, the check only guards against a connection of another kind
        if (tracking != null && con instanceof HttpsURLConnection) {
            tracking.startRequest();
            ((HttpsURLConnection) con).setSSLSocketFactory(tracking);
        }

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        con.connect();
        return con;
    }

    /**
     * Reads the response to the end and closes it, which hands the connection back to the
     * keep-alive cache of the JDK. disconnect() would close the socket instead, and the next batch
     * would need a new TCP connection and a new TLS handshake.
     *
     * @param con HTTP URL Connection
     * @param httpStatus HTTP status of the response
     */
    private static void releaseConnection(HttpURLConnection con, int httpStatus) {
        try {
            InputStream response = (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST)
                ? con.getErrorStream()
                : con.getInputStream();

            if (response != null) {
                try (InputStream stream = response) {
                    byte[] buffer = new byte[4096];
                    while (stream.read(buffer) != -1) {
                        // discard the body, only the status code matters
                    }
                }
            }
        } catch (IOException e) {
            // the batch has been answered already, only the connection is unusable now
            con.disconnect();
        }
    }

    /**
     * The HTTP consumer holds no resources of its own. Idle connections belong to the keep-alive
     * cache of the JDK, which closes them after the keep-alive timeout of the server.
     */
    @Override
    public void close() {
        // do nothing
    }

    /**
     * Decides whether it is worth sending the identical batch again.
     *
     * <p>A server error (5xx) is usually temporary. A client error (4xx) is permanent: the retry
     * would send the exact same request, the batch would never leave the queue and would block
     * everything behind it. The two exceptions are 408 and 429, which explicitly ask for a retry.</p>
     *
     * @param httpStatus HTTP status of the response
     *
     * @return boolean
     */
    static boolean isRetryableHttpStatus(int httpStatus) {
        if (httpStatus >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return true;
        }

        return httpStatus == HttpURLConnection.HTTP_CLIENT_TIMEOUT || httpStatus == HTTP_TOO_MANY_REQUESTS;
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    @Override
    protected BatchResult sendBatchContent(List<String> batchContent) {
        String payload = this.verifyPayload(batchContent);
        if (payload == null) {
            this.discardUnprocessableBatch(batchContent);

            // drop the batch instead of retrying it forever
            return BatchResult.DISCARDED;
        }

        if (payload.isEmpty()) {
            return BatchResult.RETRY;
        }

        String urlString = this.getUrl();
        long currentBatchSize = batchContent.size();
        this.logger.debug(MappIntelligenceMessages.SEND_BATCH_DATA, urlString, currentBatchSize);

        BatchResult batchSentStatus = BatchResult.SENT;
        // read once: the callback may be replaced concurrently
        SessionTrackingSocketFactory tracking = this.sessionTracking;
        HttpURLConnection con = null;
        try {
            URL url = new URL(urlString);
            con = getHttpURLConnection(url, payload, tracking);

            int httpStatus = con.getResponseCode();
            this.logger.debug(MappIntelligenceMessages.BATCH_REQUEST_STATUS, httpStatus);

            if (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                this.logger.warn(MappIntelligenceMessages.BATCH_RESPONSE_TEXT, httpStatus, "HTTP Status " + httpStatus);

                if (isRetryableHttpStatus(httpStatus)) {
                    batchSentStatus = BatchResult.RETRY;
                } else {
                    batchSentStatus = BatchResult.DISCARDED;
                    this.logger.error(
                        MappIntelligenceMessages.DISCARD_BATCH_ON_CLIENT_ERROR,
                        batchContent.size(),
                        httpStatus
                    );
                }
            }

            if (tracking != null) {
                tracking.reportSession();
            }

            releaseConnection(con, httpStatus);
        } catch (IOException e) {
            batchSentStatus = BatchResult.RETRY;
            this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());

            // never hand a broken connection back to the keep-alive cache
            if (con != null) {
                con.disconnect();
            }
        }

        return batchSentStatus;
    }

    /**
     * @param callback Receiver of the ID of the TLS session each batch was sent over
     */
    public void setOnSessionId(Consumer<byte[]> callback) {
        // a factory of its own also gives the tracked connections a keep-alive pool of their own
        this.sessionTracking = (callback == null)
            ? null
            : new SessionTrackingSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), callback);
    }
}
