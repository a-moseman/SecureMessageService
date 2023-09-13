package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Cryptography;
import org.amoseman.securemessageservice.core.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements Runnable {
    private final Cryptography CRYPTOGRAPHY;
    private final KeyPair KEY_PAIR;
    private final ServerSocket SOCKET;
    private final List<Connection> CONNECTIONS;
    private final ConcurrentLinkedQueue<Message> MESSAGE_QUEUE;
    private final ConnectionListener CONNECTION_LISTENER;
    private boolean running;

    public Server(int port) {
        CRYPTOGRAPHY = new Cryptography();
        KEY_PAIR = CRYPTOGRAPHY.generateKeyPair();
        System.out.print("[INFO] Generated RSA key pair\n");
        try {
            SOCKET = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CONNECTIONS = Collections.synchronizedList(new ArrayList<>());
        MESSAGE_QUEUE = new ConcurrentLinkedQueue<>();
        CONNECTION_LISTENER = new ConnectionListener(CRYPTOGRAPHY, KEY_PAIR.getPublic(), SOCKET, CONNECTIONS, MESSAGE_QUEUE);
        new Thread(CONNECTION_LISTENER).start();
        System.out.printf("[INFO] Started server on port %d\n", port);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            broadcast();
        }
    }


    private void broadcast() {
        if (MESSAGE_QUEUE.size() == 0) {
            return;
        }
        Message message = MESSAGE_QUEUE.remove();
        byte[] encryptedData = Base64.getDecoder().decode(message.MESSAGE.getBytes());
        byte[] decryptedData = CRYPTOGRAPHY.decrypt(encryptedData, KEY_PAIR.getPrivate());
        for (Connection connection : CONNECTIONS) {
            byte[] reEncryptedData = Base64.getEncoder().encode(CRYPTOGRAPHY.encrypt(decryptedData, connection.CLIENT_PUBLIC_KEY));
            String string = new String(reEncryptedData);
            connection.sendMessage(string);
        }
        System.out.printf("[INFO] Broadcast message from %s\n", message.SOURCE.getHostAddress());
    }
}
