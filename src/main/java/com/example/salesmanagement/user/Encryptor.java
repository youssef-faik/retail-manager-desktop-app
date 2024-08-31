package com.example.salesmanagement.user;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Encryptor {
    static String encryptPassword(String password) {

        //MessageDigest works with MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384 and SHA-512
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] messageDigest = md.digest(password.getBytes());

        BigInteger bigInt = new BigInteger(1, messageDigest);

        return bigInt.toString(16);
    }
}
