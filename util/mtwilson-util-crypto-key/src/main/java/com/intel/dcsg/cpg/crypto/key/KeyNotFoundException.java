/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

/**
 * Thrown when a specific key is required but was not found.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class KeyNotFoundException extends Exception {
    private byte[] keyId;
    
    public KeyNotFoundException(byte[] keyId) {
        super();
        this.keyId = keyId;
    }
    public KeyNotFoundException(byte[] keyId, Throwable cause) {
        super(cause);
        this.keyId = keyId;
    }
    public KeyNotFoundException(String message) {
        super(message);
    }
    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public byte[] getKeyId() {
        return keyId;
    }
    
}
