package com.nculab.kuoweilun.sdncontrollerapp.controller;

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
    //自己的Key
    private static PublicKey myPublicKey;
    private static PrivateKey privateKey;
    //對方的PublicKey
    private static PublicKey publicKey;

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

    //創造Key
    public KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public String encrypt(byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] text = cipher.doFinal(message);

        return Base64.encodeToString(text, Base64.DEFAULT);
    }

    public String decrypt(byte[] encrypted) throws Exception {
        encrypted = Base64.decode(encrypted, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] text = cipher.doFinal(encrypted);

        return new String(text);
    }
}
