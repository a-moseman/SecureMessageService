package org.amoseman.securemessageservice.core;

import org.amoseman.securemessageservice.core.cryptography.Cryptography;
import org.bouncycastle.openpgp.PGPKeyPair;

public class Application {
    public Cryptography cryptography;
    public final PGPKeyPair PGP_KEY_PAIR;

    public Application() {
        cryptography = new Cryptography();
        System.out.println("Generating PGP key pair...");
        PGP_KEY_PAIR = cryptography.generateKeyPair();
    }
}
