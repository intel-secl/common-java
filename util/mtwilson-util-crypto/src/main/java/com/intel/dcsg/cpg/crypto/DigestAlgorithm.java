/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.HexUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public enum DigestAlgorithm {
    MD5(16, "MD5"),
    SHA1(20, "SHA-1", "SHA1"), // sha1 is 160-bits
    SHA256(32, "SHA-256", "SHA256", "SHA2-256", "SHA-2-256"),
    SHA384(48, "SHA-384", "SHA384", "SHA2-384", "SHA-2-384"),// like sha512 but not vulnerable to length-extension attack due to truncated output
    SHA512(64, "SHA-512", "SHA512", "SHA2-512", "SHA-2-512");

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestAlgorithm.class);
    protected final String[] algorithmNames;
    protected final int length;
    protected final String prefix;

    DigestAlgorithm(int digestLengthBytes, String... algorithmNames) {
        this.length = digestLengthBytes;
        this.algorithmNames = algorithmNames;
        this.prefix = String.format("%s:", name().toLowerCase()); // "sha256:", "sha384:"
    }

    public String prefix() {
        return prefix;
    }

    public String algorithm() {
        return algorithmNames[0];
    }

    public int length() {
        return length;
    }

    public boolean match(String name) {
        for(int i=0; i<algorithmNames.length; i++) {
            if( algorithmNames[i].equalsIgnoreCase(name) ) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isValid(byte[] value) {
        return value != null && value.length == length;
    }

    public boolean isValidHex(String hexValue) {
        if (hexValue == null || hexValue.isEmpty()) {
            return false;
        }
        hexValue = HexUtil.trim(hexValue);
        log.debug("isValidHex: {}", hexValue);
        return (hexValue.length() == length * 2) && HexUtil.isHex(hexValue);
    }

    public boolean isValidHexWithPrefix(String digestHex) {
        if (digestHex == null || digestHex.isEmpty()) {
            return false;
        }
        String lowercaseDigestHex = digestHex.trim().toLowerCase();
        return lowercaseDigestHex.startsWith(prefix) && isValidHex(lowercaseDigestHex.substring(prefix.length()));
    }

    public boolean isValidBase64(String base64Value) {
        if (base64Value == null) {
            return false;
        }
        base64Value = Base64Util.trim(base64Value);
        return (base64Value.length() == Math.round(4*Math.ceil(1.0*length/3))) && Base64.isBase64(base64Value);
    }

    public boolean isValidBase64WithPrefix(String digestBase64) {
        if (digestBase64 == null || digestBase64.isEmpty()) {
            return false;
        }
        String lowercaseDigestBase64 = digestBase64.trim().toLowerCase();
        return lowercaseDigestBase64.startsWith(prefix) && isValidBase64(lowercaseDigestBase64.substring(prefix.length()));
    }

    /**
     * 
     * @param message must not be null
     * @return
     * @throws UnsupportedOperationException which wraps
     * NoSuchAlgorithmException, thrown if the platform doesn't have an
     * implementation for the selected algoritm
     * @throws NullPointerException or IllegalArgumentException if you pass a
     * null object
     */
    public byte[] digest(byte[] message) {
        try {
            MessageDigest hash = MessageDigest.getInstance(algorithmNames[0]); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
            byte[] digest = hash.digest(message);
            return digest;
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Missing algorithm implementation: " + algorithmNames[0], e);
        }
    }
    
}
