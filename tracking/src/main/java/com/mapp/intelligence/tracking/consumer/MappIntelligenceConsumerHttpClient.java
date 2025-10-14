package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
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
     * Constant for default connection timeout.
     */
    private final int connectionTimeout;
    /**
     * Constant for default read timeout.
     */
    private final int readTimeout;

    private Consumer<byte[]> onSessionId = null;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceConsumerHttpClient(Map<String, Object> config) {
        super(config);

        this.connectionTimeout = (int) config.getOrDefault("connectionTimeout", DEFAULT_CONNECT_TIMEOUT);
        this.readTimeout = (int) config.getOrDefault("readTimeout", DEFAULT_READ_TIMEOUT);
    }

    private HttpURLConnection getHttpURLConnection(URL url, String payload) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain; utf-8");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Keep-Alive", "timeout=1800");

        con.setConnectTimeout(this.connectionTimeout);
        con.setReadTimeout(this.readTimeout);

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        con.connect();
        return con;
    }

    /**
     * @param con HTTP URL Connection
     * @param url Connection URL
     */
    private void sendSessionId(HttpURLConnection con, URL url) {
        HttpsURLConnection connection = (HttpsURLConnection) con;
        try (SSLSocket socket = (SSLSocket) connection.getSSLSocketFactory().createSocket(url.getHost(), 443)) {
            this.onSessionId.accept(socket.getSession().getId());
        } catch (IOException e) {
            // do noting
        }
    }

    /**
     * Automatic implementation.
     */
    @Override
    protected void close() {
        // do nothing
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return boolean
     */
    @Override
    public boolean sendBatch(List<String> batchContent) {
        String payload = this.verifyPayload(batchContent);
        if (payload.isEmpty()) {
            return false;
        }

        String urlString = this.getUrl();
        long currentBatchSize = batchContent.size();
        this.logger.debug(MappIntelligenceMessages.SEND_BATCH_DATA, urlString, currentBatchSize);

        boolean batchSentStatus = true;
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = getHttpURLConnection(url, payload);

            int httpStatus = con.getResponseCode();
            this.logger.debug(MappIntelligenceMessages.BATCH_REQUEST_STATUS, httpStatus);

            if (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                this.logger.warn(MappIntelligenceMessages.BATCH_RESPONSE_TEXT, httpStatus, "HTTP Status " + httpStatus);
                batchSentStatus = false;
            }

            if (this.onSessionId != null) {
                this.sendSessionId(con, url);
            }

            con.disconnect();
        } catch (IOException e) {
            batchSentStatus = false;
            this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }

        return batchSentStatus;
    }

    /**
     * @param callback Callback for complete flushing
     */
    public void setOnSessionId(Consumer<byte[]> callback) {
        this.onSessionId = callback;
    }
}
