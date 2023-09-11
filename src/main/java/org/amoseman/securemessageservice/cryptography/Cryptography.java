package org.amoseman.securemessageservice.cryptography;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;


/**
 * https://stackoverflow.com/questions/35129959/encrypt-decrypt-files-using-bouncy-castle-pgp-in-java
 */
public class Cryptography {
    private static final int BIT_STRENGTH = 2048;
    private KeyGenerator keyGenerator;
    private Encryptor encryptor;
    private Decryptor decryptor;

    private Cryptography() {
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
