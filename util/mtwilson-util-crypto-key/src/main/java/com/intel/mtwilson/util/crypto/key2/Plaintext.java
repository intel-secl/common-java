/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 *
 * @author jbuhacoff
 */
public class Plaintext {
    private byte[] message;

    public Plaintext(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }
    
    
}
