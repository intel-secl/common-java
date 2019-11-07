/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordTest.class);
    private static final Charset utf8 = Charset.forName("UTF-8");
    
    @Test
    public void testEmptyPassword() {
        Password empty = new Password();
        assertTrue(empty.isEmpty());
        assertArrayEquals(empty.toCharArray(), new char[0]);
        assertArrayEquals(empty.toByteArray(), new byte[0]);
        assertArrayEquals(empty.toByteArray(utf8), new byte[0]);
        assertEquals("", new String(empty.toCharArray()));
    }
    
    @Test
    public void testSamplePassword() {
        String text = "1piQpnYE6U4_";
        Password password = new Password(text);
        assertEquals(12, text.length());
        assertArrayEquals(text.toCharArray(), password.toCharArray());
        
        
        CharBuffer charbuffer = CharBuffer.wrap(text.toCharArray());
        assertEquals(12, charbuffer.array().length);
        
		ByteBuffer bytebuffer = utf8.encode(CharBuffer.wrap(text.toCharArray()));
                log.debug("byte buffer position:{} hasarray:{} arrayOffset:{}  limit:{}", bytebuffer.position(), bytebuffer.hasArray(), bytebuffer.arrayOffset(), bytebuffer.limit());
                assertEquals(12, bytebuffer.limit());
                byte[] copy = Arrays.copyOf(bytebuffer.array(), bytebuffer.limit());
                assertEquals(12, copy.length);
        
        assertArrayEquals(text.getBytes(utf8), password.toByteArray(utf8));
        
        assertArrayEquals("1piQpnYE6U4_".getBytes(utf8), new Password(text.getBytes(utf8)).toByteArray(utf8));
        log.debug("Base64-encoded: {}", Base64.encodeBase64String(text.getBytes(utf8)));
        log.debug("Base64-encoded: {}", Base64.encodeBase64String(password.toByteArray(utf8)));
        log.debug("Base64-encoded with LF: {}", Base64.encodeBase64String("1piQpnYE6U4_\n".getBytes(utf8)));
        log.debug("Base64-encoded with CRLF: {}", Base64.encodeBase64String("1piQpnYE6U4_\r\n".getBytes(utf8)));
        log.debug("Base64-encoded with NULL: {}", Base64.encodeBase64String("1piQpnYE6U4_\u0000".getBytes(utf8)));
        log.debug("Base64-decoded: '{}' length: {}", Base64.decodeBase64("MXBpUXBuWUU2VTRf"), Base64.decodeBase64("MXBpUXBuWUU2VTRf").length);
        log.debug("Base64-decoded: '{}' length: {}", Base64.decodeBase64("MXBpUXBuWUU2VTRfAA=="), Base64.decodeBase64("MXBpUXBuWUU2VTRfAA==").length);
    }
}
