package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Cryptography;
import org.amoseman.securemessageservice.core.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionListener implements Runnable {
    private final Cryptography CRYPTOGRAPHY;
    private final PublicKey SERVER_PUBLIC_KEY;
    private final ServerSocket SOCKET;
    private final List<Connection> CONNECTIONS;
    private final ConcurrentLinkedQueue<Message> MESSAGE_QUEUE;

    public ConnectionListener(Cryptography cryptography, PublicKey serverPublicKey, ServerSocket socket, List<Connection> connections, ConcurrentLinkedQueue<Message> messageQueue) {
        CRYPTOGRAPHY = cryptography;
        SERVER_PUBLIC_KEY = serverPublicKey;
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
                PublicKey clientPublicKey = exchangePublicKeys(socket);
                System.out.printf("[INFO] Exchanged public keys with %s\n", socket.getInetAddress().getHostAddress());
                Connection connection = new Connection(socket, MESSAGE_QUEUE, clientPublicKey);
                CONNECTIONS.add(connection);
                new Thread(connection).start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PublicKey exchangePublicKeys(Socket socket) throws IOException {
        DataInputStream cIn = new DataInputStream(socket.getInputStream());
        DataOutputStream cOut = new DataOutputStream(socket.getOutputStream());
        cOut.write(SERVER_PUBLIC_KEY.getEncoded()); // send the client the server's public key
        byte[] clientPublicKeyBytes = cIn.readNBytes(Cryptography.RSA_2408_BIT_KEY_BYTE_LENGTH);//cIn.readUTF().getBytes(); // receive the client's public key
        return CRYPTOGRAPHY.readPublicKey(clientPublicKeyBytes);
    }
}
