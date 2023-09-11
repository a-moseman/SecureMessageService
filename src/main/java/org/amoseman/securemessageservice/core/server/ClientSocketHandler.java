package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.cryptography.Cryptography;
import org.bouncycastle.bcpg.PublicKeyPacket;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.List;

public class ClientSocketHandler implements Runnable {
    private final Socket SOCKET;
    private final List<ClientSocketHandler> CLIENT_SOCKET_HANDLERS;
    private final PGPKeyPair PGP_KEY_PAIR;
    private final Cryptography CRYPTOGRAPHY;
    private PGPPublicKey clientPGPPublicKey;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean running;

    public ClientSocketHandler(Socket clientSocket, List<ClientSocketHandler> clientSocketHandlers, PGPKeyPair pgpKeyPair, Cryptography cryptography) {
        this.SOCKET = clientSocket;
        this.CLIENT_SOCKET_HANDLERS = clientSocketHandlers;
        this.PGP_KEY_PAIR = pgpKeyPair;
        this.CRYPTOGRAPHY = cryptography;
    }

    private void initialize() throws IOException {
        inputStream = SOCKET.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        printWriter = new PrintWriter(SOCKET.getOutputStream(), true);
    }

    private void exchangePGPKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, PGPException {
        // send public key
        send(PGP_KEY_PAIR.getPublicKey().toString(), false);
        // receive client public key
        String response = bufferedReader.readLine();
        // do this bs to get client PGP public key https://stackoverflow.com/questions/24658090/how-do-i-store-and-read-pgp-public-keys-as-strings-using-bouncycastle-java
        InputStream in = new ByteArrayInputStream(response.getBytes());
        in = PGPUtil.getDecoderStream(in);
        JcaPGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(in);
        in.close();
        Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
        while (clientPGPPublicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (clientPGPPublicKey == null && kIt.hasNext()) {
                PGPPublicKey k = kIt.next();
                if (k.isEncryptionKey()) {
                    clientPGPPublicKey = k;
                }
            }
        }
    }

    public void run() {
        try {
            initialize();
            exchangePGPKeys();
        }
        catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | PGPException e) {
            e.printStackTrace();
            return;
        }

        running = true;
        String line;
        while (running) {
            try {
                line = bufferedReader.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    SOCKET.close();
                    return;
                }
                else {
                    broadcast(line);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        // close
        try {
            bufferedReader.close();
            printWriter.close();
            SOCKET.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String message, boolean encrypt) {
        if (encrypt) {
            byte[] encryptedMessage;
            try {
                encryptedMessage = CRYPTOGRAPHY.encryptData(message.getBytes(StandardCharsets.UTF_8), clientPGPPublicKey);
            } catch (PGPException | IOException e) {
                throw new RuntimeException(e);
            }
            printWriter.println(new String(encryptedMessage));
        }
        else {
            printWriter.println(message);
        }
    }

    public void broadcast(String line) {
        System.out.println(SOCKET.getInetAddress().getHostAddress() + ">" + line);
        CLIENT_SOCKET_HANDLERS.forEach(clientSocketHandler -> clientSocketHandler.send(line, true));
    }

    public void quit() {
        running = false;
    }
}
