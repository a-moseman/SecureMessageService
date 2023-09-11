package org.amoseman.securemessageservice.core;

import org.amoseman.securemessageservice.core.cryptography.Cryptography;
import org.bouncycastle.openpgp.PGPKeyPair;

public class Application {
    public final Cryptography CRYPTOGRAPHY;
    public final PGPKeyPair PGP_KEY_PAIR;

    public Application() {
        CRYPTOGRAPHY = new Cryptography();
        System.out.println("Generating PGP key pair...");
        PGP_KEY_PAIR = CRYPTOGRAPHY.generateKeyPair();
    }
}
