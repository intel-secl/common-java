/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
/**
 * Reference: http://tools.ietf.org/html/rfc5869
 *
 * Tested with HmacSHA1 and HmacSHA256 algorithms.
 * 
 * Updated to use BouncyCastle HKDFBytesGenerator.
 * 
 * @author jbuhacoff
 */
public class HKDF {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HKDF.class);
    private final String macAlgorithm;
    private final int macLength; // bytes

    /**
     *
     * @param algorithm such as "HmacSHA256" or "HmacSHA384" or "HmacSHA512"
     * @throws NoSuchAlgorithmException
     */
    public HKDF(String algorithm) throws NoSuchAlgorithmException {
        if( algorithm.equalsIgnoreCase("HmacSHA256") || algorithm.equalsIgnoreCase("SHA256")  || algorithm.equalsIgnoreCase("SHA-256") ) {
            macAlgorithm = "SHA-256";
            macLength = 32;
        }
        else if( algorithm.equalsIgnoreCase("HmacSHA384") || algorithm.equalsIgnoreCase("SHA384")  || algorithm.equalsIgnoreCase("SHA-384") ) {
            macAlgorithm = "SHA-384";
            macLength = 48;
        }
        else if( algorithm.equalsIgnoreCase("HmacSHA512") || algorithm.equalsIgnoreCase("SHA512")  || algorithm.equalsIgnoreCase("SHA-512") ) {
            macAlgorithm = "SHA-512";
            macLength = 64;
        }
        else {
            throw new UnsupportedOperationException("Only SHA-256, SHA-384, and SHA-512 are allowed");
        }
        log.debug("Initializing HKDF with digest {}", macAlgorithm);
    }
    
    private Digest createNewDigestInstance() {
        if( macAlgorithm.equalsIgnoreCase("SHA-256") ) { return new SHA256Digest(); }
        if( macAlgorithm.equalsIgnoreCase("SHA-384") ) { return new SHA384Digest(); }
        if( macAlgorithm.equalsIgnoreCase("SHA-512") ) { return new SHA512Digest(); }
        throw new IllegalStateException("Only SHA-256, SHA-384, and SHA-512 are allowed");
    }

    /**
     * NOTE: should have been called getDigestAlgorithm
     * @return
     * @deprecated
     */
    @Deprecated
    public String getMacAlgorithm() {
        return macAlgorithm;
    }

    /**
     * NOTE: should have been named getDigestLength
     * @return length of the HASH algorithm output, in BYTES
     */
    @Deprecated
    public int getMacLength() {
        return macLength;
    }

    /**
     * 
     * @return 32 for "SHA-256", 48 for "SHA-384", or 64 for "SHA-512"
     */
    public int getDigestLengthBytes() {
        return macLength;
    }
    
    /**
     * 
     * @return "SHA-256", "SHA-384", or "SHA-512"
     */
    public String getDigestAlgorithm() {
        return macAlgorithm;
    }
    
    /**
     *
     * @param ikm input keying material
     * @param length of derived key to return
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] ikm, int length) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] salt = new byte[macLength];
        Arrays.fill(salt, (byte) 0x00);
        byte[] info = new byte[0];
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param ikm input keying material
     * @param length of derived key to return
     * @param info optional context and application-specific information; must
     * not be null; can be zero-length
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] ikm, int length, byte[] info) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] salt = new byte[macLength];
        Arrays.fill(salt, (byte) 0x00);
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param salt a non-secret random value; must not be null
     * @param ikm input keying material
     * @param length of derived key to return
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] salt, byte[] ikm, int length) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] info = new byte[0];
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param salt a non-secret random value; must not be null
     * @param ikm input keying material
     * @param length of derived key to return
     * @param info optional context and application-specific information; must
     * not be null; can be zero-length
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] salt, byte[] ikm, int length, byte[] info) throws NoSuchAlgorithmException, InvalidKeyException {
        if (length > 255 * macLength) {
            throw new InvalidParameterException("length");
        }
        if (salt == null) {
            salt = new byte[macLength];
            log.debug("checking for salt byte:{}",salt);
            Arrays.fill(salt, (byte) 0x00);
        }
        HKDFParameters params = new HKDFParameters(ikm, salt, info);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(createNewDigestInstance());
        hkdf.init(params);
        byte[] okm = new byte[length];
        int generatedLength = hkdf.generateBytes(okm, 0, length);
        log.debug("Generated {} bytes, expecting {} bytes", generatedLength, length);
        return okm;
    }
}
