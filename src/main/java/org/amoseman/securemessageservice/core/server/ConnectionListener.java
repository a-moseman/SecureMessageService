package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Cryptography;
import org.amoseman.securemessageservice.core.Message;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionListener implements Runnable {
    private final Cryptography CRYPTOGRAPHY;
    private final PublicKey SERVER_RSA_PUBLIC_KEY;
    private final SecretKey SERVER_AES_SECRET_KEY;
    private final ServerSocket SOCKET;
    private final List<Connection> CONNECTIONS;
    private final ConcurrentLinkedQueue<Message> MESSAGE_QUEUE;

    public ConnectionListener(Cryptography cryptography, PublicKey serverRSAPublicKey, SecretKey serverAESSecretKey, ServerSocket socket, List<Connection> connections, ConcurrentLinkedQueue<Message> messageQueue) {
        CRYPTOGRAPHY = cryptography;
        SERVER_RSA_PUBLIC_KEY = serverRSAPublicKey;
        SERVER_AES_SECRET_KEY = serverAESSecretKey;
        SOCKET = socket;
        CONNECTIONS = connections;
        MESSAGE_QUEUE = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = SOCKET.accept();
                System.out.printf("[INFO] Accepted connection from %s\n", socket.getInetAddress().getHostAddress());
                PublicKey clientPublicKey = handshake(socket);
                Connection connection = new Connection(socket, MESSAGE_QUEUE, clientPublicKey);
                CONNECTIONS.add(connection);
                new Thread(connection).start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PublicKey handshake(Socket socket) throws IOException {
        // exchange RSA public keys
        DataInputStream cIn = new DataInputStream(socket.getInputStream());
        DataOutputStream cOut = new DataOutputStream(socket.getOutputStream());
        cOut.write(SERVER_RSA_PUBLIC_KEY.getEncoded()); // send the client the server's public key
        byte[] clientPublicKeyBytes = cIn.readNBytes(Cryptography.RSA_2408_BIT_KEY_BYTE_LENGTH); // receive the client's public key
        PublicKey clientRSAPublicKey = CRYPTOGRAPHY.readRSAPublicKey(clientPublicKeyBytes);
        System.out.printf("[INFO] Exchanged RSA public keys with %s\n", socket.getInetAddress().getHostAddress());
        // provide AES secret key
        byte[] secretKeyBytes = SERVER_AES_SECRET_KEY.getEncoded();
        byte[] encryptedSecretKeyBytes = CRYPTOGRAPHY.RSAEncrypt(secretKeyBytes, clientRSAPublicKey);
        String encryptedSecretKeyString = new String(encryptedSecretKeyBytes);
        cOut.writeUTF(encryptedSecretKeyString);
        System.out.printf("[INFO] Sent AES secret key to %s\n", socket.getInetAddress().getHostAddress());
        return clientRSAPublicKey;
    }
}
