/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.ByteArray;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class Sha256DigestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Sha256DigestTest.class);
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha256NullByteArrayConstructor() {
        Sha256Digest sha256 = new Sha256Digest((byte[])null);
        log.debug(sha256.toString());        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha256NullStringConstructor() {
        Sha256Digest sha256 = new Sha256Digest((String)null);
        log.debug(sha256.toString());        
    }

    @Test
    public void testSha256NullByteArrayValueOf() {
        Sha256Digest sha256 = Sha256Digest.valueOf((byte[])null);
        assertNull(sha256);
        log.debug(String.valueOf(sha256));        
    }
    
    @Test
    public void testSha256NullStringValueOf() {
        Sha256Digest sha256 = Sha256Digest.valueOf((String)null);
        assertNull(sha256);
        log.debug(String.valueOf(sha256));        
    }
    
    @Test
    public void testSha256plain() {
        String hex = "0000000000000000000000000000000000000000000000000000000000000000";
        Sha256Digest sha256 = new Sha256Digest(hex);
        log.debug(sha256.toString());
    }

    @Test
    public void testSha256WithPrefix() {
        String hex = "sha256:0000000000000000000000000000000000000000000000000000000000000000";
        Sha256Digest a = new Sha256Digest(hex);
        log.debug(a.toString());
        
        Sha256Digest b = Sha256Digest.valueOf(hex);
        log.debug(b.toString());
        
        log.debug("as base64 with prefix:  {}", a.toBase64WithPrefix());
        assertEquals(a.toBase64WithPrefix(),b.toBase64WithPrefix());
    }
    
    @Test
    public void testSha256WithColons() {
        String hex = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00";
        Sha256Digest sha256 = new Sha256Digest(hex);
        log.debug(sha256.toString());
    }

    @Test
    public void testSha256WithSpaces() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        Sha256Digest sha256 = new Sha256Digest(hex);
        log.debug(sha256.toString());
    }
    
    @Test
    public void testExtendHash() {
        Sha256Digest tag = Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        log.debug("is it nul? {}", tag);
        Sha256Digest extend256 = Sha256Digest.ZERO.extend(tag.toByteArray());
        Sha256Digest extend2 = Sha256Digest.digestOf(ByteArray.concat(Sha256Digest.ZERO.toByteArray(), tag.toByteArray()));
        assertEquals(extend256.toHexString(), extend2.toHexString());
    }

    @Test
    public void testEquals() {
        assertEquals(Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"), Sha256Digest.valueOfHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

}
