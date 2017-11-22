package com.nculab.kuoweilun.sdncontrollerapp;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    private static String getKeyString(Key key) {
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    public void setPublicKey(String msg) throws Exception {
        byte[] keyBytes = Base64.decode(msg.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKey = keyFactory.generatePublic(keySpec);
    }

    public String getMyPublicKey() {
        return getKeyString(myPublicKey);
    }

    public KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public byte[] encrypt(byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] text = cipher.doFinal(message);

        return text;
    }

    public byte[] decrypt(byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] text = cipher.doFinal(encrypted);

        return text;
    }
}
