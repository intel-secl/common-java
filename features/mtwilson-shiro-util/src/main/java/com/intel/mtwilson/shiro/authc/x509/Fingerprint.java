/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.x509;

import java.io.Serializable;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class Fingerprint implements Serializable {
    private static final long serialVersionUID = 8145676147L;
    private byte[] bytes;
    
    protected Fingerprint() { }
    
    public Fingerprint(byte[] bytes) {
        this.bytes = bytes;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public String getHex() {
        return Hex.encodeHexString(bytes);
    }
}
