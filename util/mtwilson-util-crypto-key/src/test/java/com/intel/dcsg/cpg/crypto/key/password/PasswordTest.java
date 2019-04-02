/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordTest.class);

    @Test
    public void testEmptyPassword() {
        Password empty = new Password();
        assertTrue(empty.isEmpty());
        assertArrayEquals(empty.toCharArray(), new char[0]);
        assertArrayEquals(empty.toByteArray(), new byte[0]);
        assertEquals("", new String(empty.toCharArray()));
    }
}
