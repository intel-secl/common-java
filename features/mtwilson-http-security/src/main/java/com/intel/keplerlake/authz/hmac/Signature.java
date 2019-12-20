/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.keplerlake.authz.hmac;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author jbuhacoff
 */
public class Signature {

    private final SecretKey secretKey;
    private final Mac mac;

    public Signature(String digestAlg, SecretKey secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        String macAlg = getMacAlgorithmName(digestAlg);
        this.mac = Mac.getInstance(macAlg); // a string like "HmacSHA26", throws NoSuchAlgorithmException
        this.secretKey = secretKey;
        mac.init(secretKey); // throws InvalidKeyException
    }

    public Signature(String digestAlg, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        String macAlg = getMacAlgorithmName(digestAlg);
        this.mac = Mac.getInstance(macAlg); // a string like "HmacSHA26", throws NoSuchAlgorithmException
        this.secretKey = new SecretKeySpec(secret, macAlg);
        mac.init(secretKey); // throws InvalidKeyException
    }
    
    public Signature(String digestAlg, char[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        String macAlg = getMacAlgorithmName(digestAlg);
        this.mac = Mac.getInstance(macAlg); // a string like "HmacSHA26", throws NoSuchAlgorithmException
        this.secretKey = new SecretKeySpec(new String(secret).getBytes(Charset.forName("UTF-8")), macAlg);
        mac.init(secretKey); // throws InvalidKeyException
    }

    private String getMacAlgorithmName(String digestAlg) {
        String normalizedAlg = digestAlg.replace("-", "").toLowerCase(); // SHA-256 becomes sha256
        switch (normalizedAlg) {
            case "sha256":
                return "HmacSHA256";
            case "sha1":
                return "HmacSHA1";
            case "md5":
                return "HmacMD5";
            default:
                throw new UnsupportedOperationException(digestAlg);
        }
    }

    public byte[] sign(byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        return mac.doFinal(message);
    }
}
