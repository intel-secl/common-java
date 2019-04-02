/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.digest;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class MessageDigestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageDigestTest.class);

    @Test(expected=NoSuchAlgorithmException.class)
    public void testIncorrectDigestAlgorithmNameSha256() throws NoSuchAlgorithmException {
        MessageDigest.getInstance("SHA256");
    }
    @Test(expected=NoSuchAlgorithmException.class)
    public void testIncorrectDigestAlgorithmNameSha512() throws NoSuchAlgorithmException {
        MessageDigest.getInstance("SHA512");
    }
    
    // can be included in a self-test module to ensure that the platform's crypto provider's message digest instances have length information
    @Test
    public void testDigestLength() throws NoSuchAlgorithmException {
//        assertEquals(16, MessageDigest.getInstance("MD2").getDigestLength()); // this is correct but we don't care to use MD2
        assertEquals(16, MessageDigest.getInstance("MD5").getDigestLength());
        assertEquals(20, MessageDigest.getInstance("SHA-1").getDigestLength());
//        assertEquals(28, MessageDigest.getInstance("SHA-224").getDigestLength());// not available on all jvms !!
        assertEquals(32, MessageDigest.getInstance("SHA-256").getDigestLength());
        assertEquals(48, MessageDigest.getInstance("SHA-384").getDigestLength());
        assertEquals(64, MessageDigest.getInstance("SHA-512").getDigestLength());
    }
     @Test
    public void testDigestAlgorithmName() throws NoSuchAlgorithmException {
//        assertEquals(16, MessageDigest.getInstance("MD2").getDigestLength());
        assertEquals("MD5", MessageDigest.getInstance("MD5").getAlgorithm());
        assertEquals("SHA-1", MessageDigest.getInstance("SHA-1").getAlgorithm());
//        assertEquals(28, MessageDigest.getInstance("SHA-224").getDigestLength()); // not available on all jvms !!
        assertEquals("SHA-256", MessageDigest.getInstance("SHA-256").getAlgorithm());
        assertEquals("SHA-384", MessageDigest.getInstance("SHA-384").getAlgorithm());
        assertEquals("SHA-512", MessageDigest.getInstance("SHA-512").getAlgorithm());
    }
   
     
     @Test
     public void testSha256Digest() {
         String xml = "<vmblob/>";
         Sha256Digest hash = Sha256Digest.digestOf(xml.getBytes(Charset.forName("UTF-8")));
         log.debug("Digest of vm blob xml is: {}  (hex)", hash.toHexString());
         log.debug("Digest of vm blob xml is: {}  (raw)", hash.toByteArray());
     }
    
}
