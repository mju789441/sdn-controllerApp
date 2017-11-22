package com.nculab.kuoweilun.sdncontrollerapp;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by Kuo Wei Lun on 2017/11/20.
 */

public class RSA {
    private static PublicKey myPublicKey;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public RSA() throws NoSuchAlgorithmException {
        KeyPair keyPair = buildKeyPair();
        myPublicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    private static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        String str = Base64.encodeToString(keyBytes, Base64.DEFAULT);
        return str;
    }

    public void setPublicKey(String pubKey) throws Exception {
        byte[] keyBytes = Base64.decode(pubKey, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKey = keyFactory.generatePublic(keySpec);
    }

    public String getMyPublicKey() {
        try {
            return getKeyString(myPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static String encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return String.valueOf(Base64.encode(cipher.doFinal(message.getBytes()), Base64.DEFAULT));
    }

    public static byte[] decrypt(byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(encrypted);
    }
}
