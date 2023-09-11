package org.amoseman.securemessageservice.core.client;

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
        // receive server public key
        String response = bufferedReader.readLine();
        // do this bs to get server PGP public key https://stackoverflow.com/questions/24658090/how-do-i-store-and-read-pgp-public-keys-as-strings-using-bouncycastle-java
        InputStream in = new ByteArrayInputStream(response.getBytes());
        in = PGPUtil.getDecoderStream(in);
        JcaPGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(in);
        in.close();
        Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
        while (serverPGPPublicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (serverPGPPublicKey == null && kIt.hasNext()) {
                PGPPublicKey k = kIt.next();
                if (k.isEncryptionKey()) {
                    serverPGPPublicKey = k;
                }
            }
        }
        // send client public key
        sendMessage(PGP_KEY_PAIR.getPublicKey().toString(), false);
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
