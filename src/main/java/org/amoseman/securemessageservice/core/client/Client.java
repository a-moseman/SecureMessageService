package org.amoseman.securemessageservice.core.client;

import org.amoseman.securemessageservice.core.Cryptography;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

public class Client implements Runnable {
    private final Cryptography CRYPTOGRAPHY;
    private final KeyPair KEY_PAIR;
    private final Socket SOCKET;
    private final DataOutputStream OUTPUT_STREAM;
    private final DataInputStream INPUT_STREAM;
    private final InputStream USER_INPUT_STREAM;
    private PublicKey serverPublicKey;
    private boolean running;

    public Client(int serverPort, String serverAddress, InputStream userInputStream) {
        CRYPTOGRAPHY = new Cryptography();
        KEY_PAIR = CRYPTOGRAPHY.generateKeyPair();
        System.out.print("[INFO] Generated RSA key pair\n");
        try {
            SOCKET = new Socket(serverAddress, serverPort);
            OUTPUT_STREAM = new DataOutputStream(SOCKET.getOutputStream());
            INPUT_STREAM = new DataInputStream(SOCKET.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        USER_INPUT_STREAM = userInputStream;
        System.out.printf("[INFO] Connected to server at %s:%d\n", SOCKET.getInetAddress().getHostAddress(), SOCKET.getPort());
    }

    @Override
    public void run() {
        try {
            serverPublicKey = exchangePublicKeys();
            System.out.printf("[INFO] Exchanged public keys with %s:%d\n", SOCKET.getInetAddress().getHostAddress(), SOCKET.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
        new Thread(() -> {
            while (running) {
                receiveMessage();
            }
        });
        Scanner scanner = new Scanner(USER_INPUT_STREAM);
        while (running) {
            String line = scanner.nextLine();
            sendMessage(line);
        }
    }

    private void sendMessage(String message) {
        try {
            byte[] encryptedData = CRYPTOGRAPHY.encrypt(message.getBytes(), serverPublicKey);
            OUTPUT_STREAM.writeBytes(Base64.getEncoder().encodeToString(encryptedData));
            OUTPUT_STREAM.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveMessage() {
        try {
            byte[] encryptedData = INPUT_STREAM.readUTF().getBytes();
            byte[] decryptedData = CRYPTOGRAPHY.decrypt(encryptedData, KEY_PAIR.getPrivate());
            System.out.println(new String(decryptedData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey exchangePublicKeys() throws IOException {
        byte[] serverPublicKeyBytes = INPUT_STREAM.readNBytes(Cryptography.RSA_2408_BIT_KEY_BYTE_LENGTH);//INPUT_STREAM.readUTF().getBytes();
        OUTPUT_STREAM.write(KEY_PAIR.getPublic().getEncoded());
        return CRYPTOGRAPHY.readPublicKey(serverPublicKeyBytes);
    }
}
