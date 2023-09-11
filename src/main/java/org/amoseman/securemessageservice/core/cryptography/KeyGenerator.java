package org.amoseman.securemessageservice.core.cryptography;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;

import java.security.*;
import java.util.Date;

public class KeyGenerator {
    private KeyPairGenerator keyPairGenerator;

    public KeyGenerator(int bitStrength) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("DH", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(bitStrength);
    }

    public PGPKeyPair generate() {
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PGPKeyPair pgpKeyPair;
        try {
            pgpKeyPair = new JcaPGPKeyPair(
                    PGPPublicKey.ELGAMAL_ENCRYPT, keyPair, new Date());
        } catch (PGPException e) {
            throw new RuntimeException(e);
        }
        return pgpKeyPair;
    }
}
