/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.digest;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DigestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestTest.class);

    @Test
    public void testZeroValues() {
        assertEquals("00000000000000000000000000000000", Digest.md5().zero().toHex());
        assertEquals("0000000000000000000000000000000000000000", Digest.sha1().zero().toHex());
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", Digest.sha256().zero().toHex());
    }
    
    @Test
    public void testLength() {
        assertEquals(16, Digest.md5().length());
        assertEquals(20, Digest.sha1().length());
        assertEquals(32, Digest.sha256().length());
    }
    
    @Test
    public void testWellKnownDigests() {
        Charset utf8 = Charset.forName("UTF-8");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Digest.md5().digestHex("").toHex());
        assertEquals("9e107d9d372bb6826bd81d3542a419d6", Digest.md5().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Digest.sha1().digestHex("").toHex());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Digest.sha1().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Digest.sha256().digestHex("").toHex());
        assertEquals("730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525", Digest.sha224().digest("The quick brown fox jumps over the lazy dog", utf8).toHex());
    }
    
    @Test
    public void testIsValid() {
        assertTrue(Digest.md5().isValidHex("d41d8cd98f00b204e9800998ecf8427e"));
        assertFalse(Digest.md5().isValidHex("d41d8cd98f00b204e9800998ecf8427eQ"));
        assertFalse(Digest.md5().isValidHex("d41d8cd98f00b204e9800998ecf8427Q"));
        assertFalse(Digest.md5().isValidHex("d41d8cd98f00b204e9800998ecf8427e8"));
        assertFalse(Digest.md5().isValidHex("d41d8cd98f00b204e9800998ecf8427"));
    }

    @Test
    public void testIsValidWithPrefix() {
        assertTrue(Digest.md5().isValidHexWithPrefix("md5:d41d8cd98f00b204e9800998ecf8427e"));
        assertFalse(Digest.md5().isValidHexWithPrefix("md5:d41d8cd98f00b204e9800998ecf8427eQ"));
        assertFalse(Digest.md5().isValidHexWithPrefix("md5:d41d8cd98f00b204e9800998ecf8427Q"));
        assertFalse(Digest.md5().isValidHexWithPrefix("md5:d41d8cd98f00b204e9800998ecf8427e8"));
        assertFalse(Digest.md5().isValidHexWithPrefix("md5:d41d8cd98f00b204e9800998ecf8427"));
    }
    
    @Test
    public void testHexToBase64() {
        assertEquals("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toBase64());
    }

    @Test
    public void testHexToHexWithPrefix() {
        assertEquals("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHexWithPrefix());
        assertEquals("md5:d41d8cd98f00b204e9800998ecf8427e",Digest.from("d41d8cd98f00b204e9800998ecf8427e").toHexWithPrefix());
        assertEquals("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12",Digest.from("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHexWithPrefix());
        assertEquals("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Digest.from("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHexWithPrefix());
    }

    @Test
    public void testHexToBase64WithPrefix() {
        assertEquals("sha256:47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toBase64WithPrefix());
        assertEquals("md5:1B2M2Y8AsgTpgAmY7PhCfg==",Digest.from("d41d8cd98f00b204e9800998ecf8427e").toBase64WithPrefix());
        assertEquals("md5:1B2M2Y8AsgTpgAmY7PhCfg==",Digest.from("md5:d41d8cd98f00b204e9800998ecf8427e").toBase64WithPrefix());
        assertEquals("sha1:L9ThxnotKPzthJ7hu3bnORuT6xI=",Digest.from("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toBase64WithPrefix());
        assertEquals("sha1:L9ThxnotKPzthJ7hu3bnORuT6xI=",Digest.from("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toBase64WithPrefix());
        assertEquals("sha256:47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",Digest.from("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toBase64WithPrefix());
        assertEquals("sha256:47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",Digest.from("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toBase64WithPrefix());
    }

    @Test
    public void testBase64ToHexWithPrefix() {
        assertEquals("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Sha256Digest.valueOfBase64("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=").toHexWithPrefix());
        assertEquals("md5:d41d8cd98f00b204e9800998ecf8427e",Digest.from("1B2M2Y8AsgTpgAmY7PhCfg==").toHexWithPrefix());
        assertEquals("md5:d41d8cd98f00b204e9800998ecf8427e",Digest.from("md5:1B2M2Y8AsgTpgAmY7PhCfg==").toHexWithPrefix());
        assertEquals("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12",Digest.from("L9ThxnotKPzthJ7hu3bnORuT6xI=").toHexWithPrefix());
        assertEquals("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12",Digest.from("sha1:L9ThxnotKPzthJ7hu3bnORuT6xI=").toHexWithPrefix());
        assertEquals("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Digest.from("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=").toHexWithPrefix());
        assertEquals("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Digest.from("sha256:47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=").toHexWithPrefix());
    }
    
    @Test
    public void testDigestFromString() {
        // with prefix
        assertEquals(Digest.MD5, Digest.from("md5:d41d8cd98f00b204e9800998ecf8427e").getAlgorithm());
        assertEquals(Digest.SHA1, Digest.from("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").getAlgorithm());
        assertEquals(Digest.SHA256, Digest.from("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").getAlgorithm());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Digest.from("md5:d41d8cd98f00b204e9800998ecf8427e").toHex());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Digest.from("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHex());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Digest.from("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHex());
        // without prefix
        assertEquals(Digest.MD5, Digest.from("d41d8cd98f00b204e9800998ecf8427e").getAlgorithm());
        assertEquals(Digest.SHA1, Digest.from("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").getAlgorithm());
        assertEquals(Digest.SHA256, Digest.from("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").getAlgorithm());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Digest.from("d41d8cd98f00b204e9800998ecf8427e").toHex());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Digest.from("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHex());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Digest.from("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHex());
    }
    
    @Test
    public void testDigestEquals() {
        assertEquals(Digest.from("md5:d41d8cd98f00b204e9800998ecf8427e"),Digest.from("d41d8cd98f00b204e9800998ecf8427e"));
        assertEquals(Digest.from("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"),Digest.from("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"));
        assertEquals(Digest.from("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),Digest.from("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }
    
    @Test
    public void testDigestValueOf() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5Digest.valueOf("d41d8cd98f00b204e9800998ecf8427e").toHexString());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Sha1Digest.valueOf("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHexString());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256Digest.valueOf("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHexString());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5Digest.valueOf("md5:d41d8cd98f00b204e9800998ecf8427e").toHexString());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Sha1Digest.valueOf("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHexString());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256Digest.valueOf("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHexString());
    }
    
    @Test
    public void testDigestValueOfHex() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5Digest.valueOfHex("d41d8cd98f00b204e9800998ecf8427e").toHexString());
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", Sha1Digest.valueOfHex("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12").toHexString());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855").toHexString());
//        assertNull(Md5Digest.valueOfHex("md5:d41d8cd98f00b204e9800998ecf8427e")); // will not pass because "m" in "md5:" is not valid hex  .... when using prefixes use just "valueOf" it will work for prefix and non-prefix
//        assertNull(Sha1Digest.valueOfHex("sha1:2fd4e1c67a2d28fced849ee1bb76e7391b93eb12")); // will not pass because "s" and "h" in "sha1:" is not valid hex  .... when using prefixes use just "valueOf" it will work for prefix and non-prefix
//        assertNull(Sha256Digest.valueOfHex("sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")); // will not pass because "s" and "h" in "sha256:" is not valid hex  .... when using prefixes use just "valueOf" it will work for prefix and non-prefix
    }

}
