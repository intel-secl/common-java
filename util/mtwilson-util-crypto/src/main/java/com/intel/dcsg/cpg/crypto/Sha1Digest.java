/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.HexUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a SHA-1 digest. A SHA-1 digest is a 20-byte value.
 * <p>
 * When serializing, the default is to represent the digest as a hex string.
 * When deserializing, both hex and base-64 strings are recognized by {@link #valueOf(String)} but you can also
 * call more specific methods {@link #valueOfHex(String)} and {@link #valueOfBase64(String)} or pass in an 
 * existing byte array with {@link #valueOf(byte[])}.
 * <p>
 * The value methods take a representation and return an equivalent Sha1Digest instance. The static 
 * {@link #digestOf(byte[])} method computes the digest of the given byte array and returns a Sha1Digest instance
 * as the result.
 * <p>
 * The {@link #extend(byte[])} and {@link #extend(com.intel.dcsg.cpg.crypto.Sha1Digest)} methods return a new
 * Sha1Digest instance whose value is equal to SHA-1(object-value||argument-value). The original object is
 * not modified.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Sha1Digest extends AbstractDigest {
    private static final DigestAlgorithm ALGORITHM = DigestAlgorithm.SHA1;
    
    /**
     * @since 0.1.2
     */
    public final static Sha1Digest ZERO = new Sha1Digest(new byte[] {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0});
    
    /**
     * This constructor exists so the valueOf methods can instantiate an SHA1 object
     * and set its value after validating the input. If they were to call the public
     * constructors, the validation would happen again there redundantly.
     */
    protected Sha1Digest() {
        super(ALGORITHM);
    }
    
    /**
     * Use this constructor if you expect that the bytes are a valid SHA1 digest.
     * If they are not a valid SHA1 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha1Digest where the input may be invalid, use valueOf.
     * 
     * 
     * @param value 
     */
    public Sha1Digest(byte[] value) {
        super(ALGORITHM, value);
    }
    
    /**
     * Use this constructor if you expect that the hex string represents a valid SHA1 digest.
     * If it does not look like an SHA1 digest an IllegalArgumentException exception will be thrown.
     * 
     * If you need to create a Sha1Digest where the input may be invalid, use valueOf.
     * 
     * @param hex must be 40 hex characters representing 20 bytes
     */
    public Sha1Digest(String hex) {
        super(ALGORITHM, hex);
    }
    
    /**
     * Creates a NEW instance of Sha1Digest that contains the result of 
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     * @since 0.1.2
     */
    public Sha1Digest extend(Sha1Digest data) {
        return extend(data.toByteArray());
    }

    /**
     * Creates a NEW instance of Sha1Digest that contains the result of 
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     * @since 0.1.2
     */
    public Sha1Digest extend(byte[] data) {
        try {
            MessageDigest hash = MessageDigest.getInstance(algorithm());
            hash.update(toByteArray());
            hash.update(data);
            return new Sha1Digest(hash.digest());
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm: "+algorithm(), e);
        }
    }
    
    
    /**
     * 
     * @param value can be null
     * @return true if value is a valid SHA1 digest
     */
    public static boolean isValid(byte[] value) {
        return ALGORITHM.isValid(value);
    }
    
    /**
     * @param hex without any punctuation or spaces; can be null
     * @return true if the value is a valid hex representation of an SHA1 digest
     */
    public static boolean isValidHex(String hexValue) {
        return ALGORITHM.isValidHex(hexValue);
    }

    /**
     * @param base64 value without any punctuation or spaces; can be null
     * @return true if the value is a valid base64 representation of an SHA256 digest
     */
    public static boolean isValidBase64(String base64Value) {
        return ALGORITHM.isValidBase64(base64Value);
    }
    
    /**
     * Assumes the input represents an SHA1 digest and creates a new instance of Sha1Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA1 representation, a null
     * will be returned.
     * 
     * Callers must always check the return value for null. 
     * 
     * @param digest
     * @return 
     */
    public static Sha1Digest valueOf(byte[] digest) {
        if( isValid(digest) ) {
            Sha1Digest SHA1 = new Sha1Digest();
            SHA1.value = digest;
            return SHA1;
        }
        return null;
    }

    /**
     * Interprets a hex or base64-encoded value and returns a Sha1Digest instance with that value.
     * <p> 
     * This method does NOT compute a digest. If the input is not a valid SHA1 representation, a null
     * will be returned.
     * <p>
     * Callers must always check the return value for null. 
     * <p>
     * Starting with version 0.1.2 this method also allows base64 input.
     * 
     * @param text can be either hex or base-64 encoded representation of a SHA-1 digest (20 bytes)
     * @return a Sha1Digest instance or null if the input was not a valid SHA-1 representation
     */
    //@org.codehaus.jackson.annotate.JsonCreator // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonCreator // jackson 2.x
    public static Sha1Digest valueOf(String text) {
        if( text == null || text.isEmpty() ) {
            return null;
        }
        if( isValidHex(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        if( isValidBase64(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        throw new IllegalArgumentException("Invalid SHA1 digest value");
    }
    
    /**
     * @since 0.1.2
     * @param text
     * @return 
     */
    public static Sha1Digest valueOfHex(String text) {
        if( text == null || text.isEmpty() ) {
            return null;
        }
        if( isValidHex(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        throw new IllegalArgumentException("Invalid hex-encoded SHA1 digest value");
    }

    /**
     * @since 0.1.2
     * @param text
     * @return 
     */
    public static Sha1Digest valueOfBase64(String text) {
        if( text == null || text.isEmpty() ) {
            return null;
        }
        if( isValidBase64(text) ) {
            Sha1Digest digest = new Sha1Digest();
            digest.value = Base64Util.toByteArray(text); 
            return digest;
        }
        throw new IllegalArgumentException("Invalid base64-encoded SHA1 digest value");
    }
    
    
    /**
     * Computes the SHA1 digest of the input and returns the result.
     * @param message
     * @return 
     */
    public static Sha1Digest digestOf(byte[] message) {
        return new Sha1Digest(ALGORITHM.digest(message));
    }
}
