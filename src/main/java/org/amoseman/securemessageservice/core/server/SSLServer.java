package org.amoseman.securemessageservice.core.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SSLServer {
    private final SSLServerSocket SOCKET;
    private final List<Connection> CONNECTIONS;
    private final ConcurrentLinkedQueue<String> MESSAGE_QUEUE;
    private boolean running;

    public SSLServer(int port) {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try {
            SOCKET = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            SOCKET.setNeedClientAuth(true);
            SOCKET.setEnabledCipherSuites(new String[]{"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256"});
            SOCKET.setEnabledProtocols(new String[]{"TLSv1.2"});
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        CONNECTIONS = new ArrayList<>();
        MESSAGE_QUEUE = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        running = true;
        while (running) {
            broadcast();
            waitForConnection();
        }
    }

    private void broadcast() {
        if (MESSAGE_QUEUE.size() > 0) {
            CONNECTIONS.forEach((connection -> connection.send(MESSAGE_QUEUE.remove())));
        }
    }

    private void waitForConnection() {
        try {
            SSLSocket client = (SSLSocket) SOCKET.accept();
            Connection connection = new Connection(client, MESSAGE_QUEUE);
            CONNECTIONS.add(connection);
            new Thread(connection).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
