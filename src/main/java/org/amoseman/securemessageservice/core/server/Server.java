package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Cryptography;
import org.amoseman.securemessageservice.core.Message;

import javax.crypto.SecretKey;
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
    private final KeyPair RSA_KEY_PAIR;
    private final SecretKey AES_SECRET_KEY;
    private final ServerSocket SOCKET;
    private final List<Connection> CONNECTIONS;
    private final ConcurrentLinkedQueue<Message> MESSAGE_QUEUE;
    private final ConnectionListener CONNECTION_LISTENER;
    private boolean running;

    public Server(int port) {
        CRYPTOGRAPHY = new Cryptography();
        RSA_KEY_PAIR = CRYPTOGRAPHY.generateRSAKeyPair();
        System.out.print("[INFO] Generated RSA key pair\n");
        AES_SECRET_KEY = CRYPTOGRAPHY.generateAESSecretKey();
        System.out.print("[INFO] Generated AES secret key\n");

        try {
            SOCKET = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CONNECTIONS = Collections.synchronizedList(new ArrayList<>());
        MESSAGE_QUEUE = new ConcurrentLinkedQueue<>();
        CONNECTION_LISTENER = new ConnectionListener(CRYPTOGRAPHY, RSA_KEY_PAIR.getPublic(), AES_SECRET_KEY, SOCKET, CONNECTIONS, MESSAGE_QUEUE);
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
        byte[] decryptedData = CRYPTOGRAPHY.AESDecrypt(encryptedData, AES_SECRET_KEY);
        for (Connection connection : CONNECTIONS) {
            byte[] reEncryptedData = Base64.getEncoder().encode(CRYPTOGRAPHY.AESEncrypt(decryptedData, AES_SECRET_KEY));
            String string = new String(reEncryptedData);
            connection.sendMessage(string);
        }
        System.out.printf("[INFO] Broadcast message from %s\n", message.SOURCE.getHostAddress());
    }
}
