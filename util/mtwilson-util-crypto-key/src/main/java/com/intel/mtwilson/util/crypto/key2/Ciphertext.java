/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 *
 * @author jbuhacoff
 */
public class Ciphertext {
    private byte[] encrypted;
    private CipherKeyAttributes cipherKey;

    public Ciphertext(byte[] encrypted, CipherKeyAttributes cipherKey) {
        this.encrypted = encrypted;
        this.cipherKey = cipherKey;
    }

    public CipherKeyAttributes getCipherKey() {
        return cipherKey;
    }

    public byte[] getEncrypted() {
        return encrypted;
    }
    
    
}
