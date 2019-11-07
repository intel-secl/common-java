/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file.model;

/**
 * @author jbuhacoff
 */
public class UserHmacSecretKey {
    private String username;
    private String algorithm; // HMAC
    private String digestAlgorithm; // comma-separated list of allowed algorithms, for EXAMPLE "SHA256,SHA384"
    private byte[] secretKeyBytes;

    public UserHmacSecretKey(String username, String algorithm, String digestAlgorithm, byte[] secretKeyBytes) {
        this.username = username;
        this.algorithm = algorithm;
        this.digestAlgorithm = digestAlgorithm;
        this.secretKeyBytes = secretKeyBytes;
    }

    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public byte[] getSecretKeyBytes() {
        return secretKeyBytes;
    }

    public void setSecretKeyBytes(byte[] secretKeyBytes) {
        this.secretKeyBytes = secretKeyBytes;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    
    
}
