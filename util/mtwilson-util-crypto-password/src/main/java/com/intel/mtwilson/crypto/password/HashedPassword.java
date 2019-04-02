/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.password;

/**
 * 
 * @author jbuhacoff
 */
public class HashedPassword implements HashProtection {
    private byte[] passwordHash;
    private byte[] salt;
    private int iterations;
    private String algorithm;
    
    public byte[] getPasswordHash() {
        return passwordHash;
    }
   
    @Override
    public byte[] getSalt() {
        return salt;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


}

