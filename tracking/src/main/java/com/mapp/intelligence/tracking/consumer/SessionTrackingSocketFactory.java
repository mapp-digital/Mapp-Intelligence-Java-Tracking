package com.mapp.intelligence.tracking.consumer;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Remembers the SSL sockets it creates, so the session of the connection a batch was actually
 * sent over can be reported. A connection from the keep-alive cache creates no socket.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
final class SessionTrackingSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory delegate;
    private final Consumer<byte[]> callback;
    /**
     * The JDK creates the socket in the thread that sends the request.
     */
    private final ThreadLocal<SSLSocket> createdForRequest = new ThreadLocal<>();
    private volatile SSLSocket lastCreated = null;

    SessionTrackingSocketFactory(SSLSocketFactory delegate, Consumer<byte[]> callback) {
        this.delegate = delegate;
        this.callback = callback;
    }

    void startRequest() {
        this.createdForRequest.remove();
    }

    /**
     * Must be called after the response arrived: the handshake is complete then, so
     * getSession() cannot start a new one.
     */
    void reportSession() {
        SSLSocket socket = this.createdForRequest.get();
        this.createdForRequest.remove();

        if (socket == null) {
            // the connection came from the keep-alive cache, with a single sending thread
            // that is the most recent one
            socket = this.lastCreated;
        }

        if (socket == null || socket.isClosed()) {
            return;
        }

        SSLSession session = socket.getSession();
        this.callback.accept(session.getId());
    }

    private Socket track(Socket socket) {
        if (socket instanceof SSLSocket) {
            this.createdForRequest.set((SSLSocket) socket);
            this.lastCreated = (SSLSocket) socket;
        }

        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return this.track(this.delegate.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return this.track(this.delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return this.track(this.delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return this.track(this.delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.track(this.delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.track(this.delegate.createSocket(address, port, localAddress, localPort));
    }
}
