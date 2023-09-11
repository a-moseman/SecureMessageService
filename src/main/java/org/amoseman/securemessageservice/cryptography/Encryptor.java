package org.amoseman.securemessageservice.cryptography;

import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyConverter;
import org.bouncycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;

/**
 * https://stackoverflow.com/questions/61826843/java-pgp-encryption-using-bouncy-castle
 */
public class Encryptor {
    public byte[] encrypt(byte[] data, PGPPublicKey publicKey) throws IOException, PGPException {
        PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                        .setSecureRandom(new SecureRandom())
                        .setWithIntegrityPacket(true)
                        .setProvider("BC")
        );
        generator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        OutputStream cOut = generator.open(encOut, new byte[4096]);
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream pOut = literalDataGenerator.open(
                cOut, PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE, data.length, new Date());
        pOut.write(data);
        pOut.close();
        cOut.close();
        return encOut.toByteArray();
    }
}
