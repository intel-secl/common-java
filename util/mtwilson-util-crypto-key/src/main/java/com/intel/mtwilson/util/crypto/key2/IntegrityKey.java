/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 * This really refers to a MAC or HMAC key since a regular hash function doesn't
 * need a key.
 * @author jbuhacoff
 */
public class IntegrityKey extends IntegrityKeyAttributes {
    private byte[] encoded;
    


    
    /**
     * The encoded key, in the format specified by its attributes
     */

    
    public byte[] getEncoded() {
        return encoded;
    }

    public void setEncoded(byte[] encoded) {
        this.encoded = encoded;
    }


    
    @Override
    public IntegrityKey copy() {
        IntegrityKey newInstance = new IntegrityKey();
        newInstance.copyFrom(this);
        return newInstance;
    }
    
    public void copyFrom(IntegrityKey source) {
        super.copyFrom(source);
        this.encoded = source.encoded;
    }

}
