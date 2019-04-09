/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.key2;

import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class CipherKey extends CipherKeyAttributes {
    private byte[] encoded;
    

    public void clear() {
        // encoded key
        if( encoded != null ) {
            Arrays.fill(encoded, (byte)0);
        }
        // cipher attributes
        setAlgorithm(null);
        setKeyId(null);
        setKeyLength(null);
        setMode(null);
        setPaddingMode(null);
        // all extended attributes
        map().clear();
    }
    
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
    public CipherKey copy() {
        CipherKey newInstance = new CipherKey();
        newInstance.copyFrom(this);
        return newInstance;
    }
    
    public void copyFrom(CipherKey source) {
        super.copyFrom(source);
        this.encoded = source.encoded;
    }
}
