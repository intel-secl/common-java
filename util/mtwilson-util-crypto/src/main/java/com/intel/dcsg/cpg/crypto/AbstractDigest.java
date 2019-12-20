/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.HexUtil;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * @since 0.1
 * @author jbuhacoff
 */
public class AbstractDigest {
    protected DigestAlgorithm algorithm;
    protected byte[] value;
    
    protected AbstractDigest(DigestAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    protected AbstractDigest(DigestAlgorithm algorithm, byte[] value) {
        if(!algorithm.isValid(value)) {
            throw new IllegalArgumentException("Invalid "+algorithm.name()+" digest: "+(value==null?"null":Hex.encodeHexString(value)));
        }
        this.algorithm = algorithm;
        this.value = value;
    }
    
    protected AbstractDigest(DigestAlgorithm algorithm, String hexValue) {
        if(algorithm.isValidHexWithPrefix(hexValue)) {
            hexValue = hexValue.substring(algorithm.prefix().length());
            this.algorithm = algorithm;
            this.value = HexUtil.toByteArray(hexValue);        
        }
        else if(algorithm.isValidHex(hexValue)) {
            this.algorithm = algorithm;
            this.value = HexUtil.toByteArray(hexValue);        
        }
        else {
            throw new IllegalArgumentException("Invalid "+algorithm.name()+" digest: "+(hexValue==null?"null":hexValue));
        }
    }

    public String algorithm() {
        return algorithm.name(); // MD5, SHA1, SHA256  ... XXX or do we want to return the java name MD5, SHA-1, SHA-256 ?  that one is in algorithm.algorithm()
    }
    
    public byte[] toByteArray() {
        return value;
    }

    /**
     * XXX should this be renamed just toHex() ? by definition hex is a string since it's a textual representation of
     * binary data
     * 
     * @return 
     */
    public String toHexString() {
        return Hex.encodeHexString(value);
    }

    public String toBase64() {
        return Base64.encodeBase64String(value);
    }
    
    public String toHexWithPrefix() {
        return String.format("%s%s", algorithm.prefix(), toHexString());
    }

    public String toBase64WithPrefix() {
        return String.format("%s%s", algorithm.prefix(), toBase64());
    }
    
    //@org.codehaus.jackson.annotate.JsonValue // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonValue // jackson 2.x
    @Override
    public String toString() {
        return Hex.encodeHexString(value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    /**
     * Performs a value-comparison on the two digests; two digests are equal if
     * they are instances of the same class and their values (byte arrays sized
     * for the digest length) are equal. 
     * The Java built-in Arrays.equals() method is used to compare the contents
     * and it enforces the byte arrays are the same length and have the same
     * elements in the same order.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDigest other = (AbstractDigest) obj;
        if (!Arrays.equals(value, other.value)) {
            return false;
        }
        return true;
    }
}
