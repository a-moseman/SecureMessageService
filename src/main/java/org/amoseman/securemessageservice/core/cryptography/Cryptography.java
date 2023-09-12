package org.amoseman.securemessageservice.core.cryptography;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

/**
 * https://stackoverflow.com/questions/35129959/encrypt-decrypt-files-using-bouncy-castle-pgp-in-java
 */
public class Cryptography {
    private static final int BIT_STRENGTH = 2048;
    private KeyGenerator keyGenerator;
    private Encryptor encryptor;
    private Decryptor decryptor;

    public Cryptography() {
        keyGenerator = new KeyGenerator(BIT_STRENGTH);
        encryptor = new Encryptor();
        decryptor = new Decryptor();
    }

    public PGPKeyPair generateKeyPair() {
        return keyGenerator.generate();
    }

    public byte[] encryptData(byte[] data, PGPPublicKey publicKey) throws PGPException, IOException {
        return encryptor.encrypt(data, publicKey);
    }

    public byte[] decryptData(byte[] data, PGPPrivateKey privateKey) throws PGPException, IOException {
        return decryptor.decrypt(data, privateKey);
    }

    public String encodeAsciiArmorPublicKey(PGPPublicKey publicKey) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArmoredOutputStream armored = new ArmoredOutputStream(out);
        publicKey.encode(armored);
        armored.close();
        return out.toString(StandardCharsets.US_ASCII);
    }

    public PGPPublicKey readPublicKey(String string) throws IOException, PGPException {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes());
        PGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(PGPUtil.getDecoderStream(inputStream));
        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            Iterator keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();
                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException, PGPException {
        // example usage
        Cryptography cryptography = new Cryptography();
        PGPKeyPair aKeyPair = cryptography.generateKeyPair();
        PGPKeyPair bKeyPair = cryptography.generateKeyPair();

        String message = "Hello world! This is a secret message.";
        byte[] cipherText = cryptography.encryptData(message.getBytes(StandardCharsets.UTF_8), bKeyPair.getPublicKey());
        byte[] decipheredText = cryptography.decryptData(cipherText, bKeyPair.getPrivateKey());
        System.out.println(new String(decipheredText));
    }
}
