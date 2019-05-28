/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.key2;

import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.io.Copyable;

/**
 * Extensible with new attributes via the attributes map and Jackson's
 * annotations JsonAnyGetter and JsonAnySetter.  
 * 
 * @author jbuhacoff
 */
public class CipherKeyAttributes extends Attributes implements Copyable {
    private String keyId;
    private String algorithm;
    private Integer keyLength;
    private String mode;
    private String paddingMode;
    
    /**
     * The key id can be used to look up the key in a database or 
     * key server when the encoded key is not present
     */
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
    
    /**
     * For a certificate, refers to the algorithm of the enclosed public key.
     * Examples: AES, RSA
     */
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    
    /**
     * In bits, and always refers to the plaintext key without any
     * encoding or encryption. For certificates, this refers to the
     * length of the enclosed public key. 
     * Examples of key length for AES: 128, 256
     * Examples of key length for RSA: 1024, 2048, 3072
     */
    public Integer getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }
    
    
    
    /**
     * Cipher mode of operation for this key. 
     * Using the same key for multiple modes is strongly not recommended
     * because it could aid cryptanalysis. Therefore the cipher mode is
     * required so that all users of the key know which cipher mode should
     * be used with it.
     * Examples: CBC, OFB
     */    
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    
    /**
     * Padding mode used with this key. For example: ANSIX923, ISO10126,
     * None, PKCS7, Zeros, PKCS15, OAEP.
     * Examples: 
     */
    public String getPaddingMode() {
        return paddingMode;
    }

    public void setPaddingMode(String paddingMode) {
        this.paddingMode = paddingMode;
    }

    @Override
    public CipherKeyAttributes copy() {
        CipherKeyAttributes newInstance = new CipherKeyAttributes();
        newInstance.copyFrom(this);
        return newInstance;
    }
    
    public void copyFrom(CipherKeyAttributes source) {
        super.copyFrom(source);
        this.keyId = source.keyId;
        this.algorithm = source.algorithm;
        this.keyLength = source.keyLength;
        this.mode = source.mode;
        this.paddingMode = source.paddingMode;
    }

}
