package org.amoseman.securemessageservice.core.cryptography;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.util.io.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * https://stackoverflow.com/questions/61826843/java-pgp-encryption-using-bouncy-castle
 */
public class Decryptor {
    public byte[] decrypt(byte[] data, PGPPrivateKey privateKey) throws IOException, PGPException {
        PGPObjectFactory pgpFact = new JcaPGPObjectFactory(data);
        PGPEncryptedDataList encList = (PGPEncryptedDataList) pgpFact.nextObject();
        Iterator<PGPEncryptedData> iter = encList.getEncryptedDataObjects();
        PGPPublicKeyEncryptedData encData = null;
        while (iter.hasNext()) {
            PGPEncryptedData pgpEncData = iter.next();
            PGPPublicKeyEncryptedData pkEnc = (PGPPublicKeyEncryptedData) pgpEncData;
            if (pkEnc.getKeyID() == privateKey.getKeyID()) {
                encData = pkEnc;
                break;
            }
        }
        if (encData == null) {
            throw new IllegalStateException("Matching encrypted data not found.");
        }

        PublicKeyDataDecryptorFactory dataDecryptorFactory = new JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider("BC")
                .build(privateKey);

        InputStream inputStream = encData.getDataStream(dataDecryptorFactory);
        byte[] literalData = Streams.readAll(inputStream);
        inputStream.close();

        if (encData.verify()) {
            PGPObjectFactory litFact = new JcaPGPObjectFactory(literalData);
            PGPLiteralData litData = (PGPLiteralData) litFact.nextObject();
            return Streams.readAll(litData.getInputStream());
        }
        throw new IllegalStateException("Modification check failed.");
    }
}
