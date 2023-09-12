package org.amoseman.securemessageservice.core.client.deprecated;

import org.amoseman.securemessageservice.core.cryptography.Cryptography;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;

public class SocketHandler implements Runnable {
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    private final PGPKeyPair PGP_KEY_PAIR;
    private final Cryptography CRYPTOGRAPHY;
    private PGPPublicKey serverPGPPublicKey;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private boolean running;

    public SocketHandler(String serverAddress, int serverPort, PGPKeyPair pgpKeyPair, Cryptography cryptography) {
        this.SERVER_ADDRESS = serverAddress;
        this.SERVER_PORT = serverPort;
        this.PGP_KEY_PAIR = pgpKeyPair;
        this.CRYPTOGRAPHY = cryptography;
    }

    private void initialize() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void exchangePGPKeys() throws IOException, PGPException {
        System.out.println("Exchanging public keys...");
        // receive server public key
        String response = bufferedReader.readLine();
        serverPGPPublicKey = CRYPTOGRAPHY.readPublicKey(response);
        if (serverPGPPublicKey == null) {
            throw new IllegalStateException("Failed to receive proper public PGP key.");
        }
        System.out.println("Received server public key.");
        // send client public key
        sendMessage(PGP_KEY_PAIR.getPublicKey().toString(), false);
        System.out.println("Sent public key to server.");
    }

    public void run() {
        try {
            initialize();
            exchangePGPKeys();
        } catch (IOException | PGPException e) {
            throw new RuntimeException(e);
        }
        running = true;
        while (running) {
            try {
                String line = bufferedReader.readLine();
                System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message, boolean encrypt) {
        if (encrypt) {
            byte[] encryptedMessage;
            try {
                encryptedMessage = CRYPTOGRAPHY.encryptData(message.getBytes(), serverPGPPublicKey);
            } catch (PGPException | IOException e) {
                throw new RuntimeException(e);
            }
            printWriter.println(new String(encryptedMessage));
        }
        else {
            printWriter.println(message);
        }
    }

    public void quit() {
        running = false;
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
