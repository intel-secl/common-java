/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.hmac;

import java.io.Serializable;

/**
 *
 * @author jbuhacoff
 */
public class HmacAuthenticationCredential implements Serializable {
    private static final long serialVersionUID = 81145183L;
    
    private final byte[] message;
    private final String digestAlg;
    private final byte[] signature;
    
    public HmacAuthenticationCredential(byte[] message, String digestAlg, byte[] signature) {
        this.message = message;
        this.digestAlg = digestAlg;
        this.signature = signature;
    }
    
    /**
     * by the secret key over the message
     * @return 
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * The signed message reconstructed from the request
     * @return 
     */
    public byte[] getMessage() {
        return message;
    }

    public String getDigestAlg() {
        return digestAlg;
    }
    
    
    
}
