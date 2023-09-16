package org.amoseman.securemessageservice.core;

import javax.crypto.*;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Cryptography {
    public static final int RSA_2408_BIT_KEY_BYTE_LENGTH = 294;
    private final KeyPairGenerator RSA_KEY_PAIR_GENERATOR;
    private final KeyGenerator AES_KEY_PAIR_GENERATOR;

    public Cryptography() {
        try {
            RSA_KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("RSA");
            RSA_KEY_PAIR_GENERATOR.initialize(2048);
            AES_KEY_PAIR_GENERATOR = KeyGenerator.getInstance("AES");
            AES_KEY_PAIR_GENERATOR.init(256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public KeyPair generateRSAKeyPair() {
        return RSA_KEY_PAIR_GENERATOR.generateKeyPair();
    }

    public SecretKey generateAESSecretKey() {
        return AES_KEY_PAIR_GENERATOR.generateKey();
    }

    public byte[] RSAEncrypt(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] RSADecrypt(byte[] data, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] AESEncrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] AESDecrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey readRSAPublicKey(byte[] data) {
        try {
            KeyFactory keyFactory  = KeyFactory.getInstance("RSA");
            EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(data);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
