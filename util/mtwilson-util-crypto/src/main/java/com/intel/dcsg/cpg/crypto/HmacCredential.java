/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class HmacCredential implements Credential {
    private static Logger log = LoggerFactory.getLogger(HmacCredential.class);
    private final String username;
    private final Password password;
    private final String signatureAlgorithm = "HmacSHA256";
    private final byte[] identity;
    
    public HmacCredential(String clientId, String secretKey) {
        username = clientId;
        password = new Password(secretKey);
        identity = getIdentity(clientId);
    }
    public HmacCredential(String clientId, Password secretKey) {
        username = clientId;
        password = secretKey;
        identity = getIdentity(clientId);
    }

    
    private byte[] getIdentity(String name) {
        try {
            return name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.toString(), ex);
        }
        return name.getBytes();
    }
    
    /**
     * If UTF-8 encoding is not available, returns the bytes in the default platform encoding
     * @return UTF-8 encoded username API such as "cloudsecurity@intel"
     */
    @Override
    public byte[] identity() {
        return identity;
    }
    
    /**
     * If document is text, you should pass it in the encoding you want to sign,
     * such as document.getBytes("UTF-8").
     * 
     * The Credential interface indicates that signature() may throw a SignatureException,
     * but this implementation does not throw SignatureException.
     * 
     * @param document
     * @return 
     */
    @Override
    public byte[] signature(byte[] document) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec(password.toByteArray(), signatureAlgorithm);
        Mac mac = Mac.getInstance(signatureAlgorithm); // a string like "HmacSHA256"
        mac.init(key);
        return mac.doFinal(document);
    }
    
    /**
     * 
     * @return the signature algorithm "HmacSHA256"
     */
    @Override
    public String algorithm() {
        return signatureAlgorithm;
    }
}
