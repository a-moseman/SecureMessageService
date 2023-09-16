package org.amoseman.securemessageservice.core.client;

import org.amoseman.securemessageservice.core.Cryptography;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

public class Client implements Runnable {
    private final Cryptography CRYPTOGRAPHY;
    private final KeyPair RSA_KEY_PAIR;
    private final Socket SOCKET;
    private final DataOutputStream OUTPUT_STREAM;
    private final DataInputStream INPUT_STREAM;
    private final InputStream USER_INPUT_STREAM;
    private PublicKey serverRSAPublicKey;
    private SecretKey serverAESSecretKey;
    private boolean running;

    public Client(int serverPort, String serverAddress, InputStream userInputStream) {
        CRYPTOGRAPHY = new Cryptography();
        RSA_KEY_PAIR = CRYPTOGRAPHY.generateRSAKeyPair();
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
            handshake();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
        new Thread(() -> {
            while (running) {
                receiveMessage();
            }
        }).start();
        Scanner scanner = new Scanner(USER_INPUT_STREAM);
        while (running) {
            String line = scanner.nextLine();
            sendMessage(line);
        }
    }

    private void sendMessage(String message) {
        try {
            byte[] encryptedData = Base64.getEncoder().encode(CRYPTOGRAPHY.AESEncrypt(message.getBytes(), serverAESSecretKey));
            OUTPUT_STREAM.writeUTF(new String(encryptedData));
            OUTPUT_STREAM.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveMessage() {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(INPUT_STREAM.readUTF());
            byte[] decryptedData = CRYPTOGRAPHY.AESDecrypt(encryptedData, serverAESSecretKey);
            System.out.println(new String(decryptedData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handshake() throws IOException {
        // exchange RSA public keys
        byte[] serverPublicKeyBytes = INPUT_STREAM.readNBytes(Cryptography.RSA_2408_BIT_KEY_BYTE_LENGTH);
        OUTPUT_STREAM.write(RSA_KEY_PAIR.getPublic().getEncoded());
        serverRSAPublicKey = CRYPTOGRAPHY.readRSAPublicKey(serverPublicKeyBytes);
        System.out.printf("[INFO] Exchanged RSA public keys with %s:%d\n", SOCKET.getInetAddress().getHostAddress(), SOCKET.getPort());
        // receive AES secret key
        byte[] encryptedAESSecretKeyBytes = INPUT_STREAM.readUTF().getBytes();
        byte[] decryptedAESSecretKeyBytes = CRYPTOGRAPHY.RSADecrypt(encryptedAESSecretKeyBytes, RSA_KEY_PAIR.getPrivate());
        serverAESSecretKey = new SecretKeySpec(decryptedAESSecretKeyBytes, 0, decryptedAESSecretKeyBytes.length, "AES");
        System.out.printf("[INFO] Received AES secret key from %s:%d\n", SOCKET.getInetAddress().getHostAddress(), SOCKET.getPort());
    }
}
