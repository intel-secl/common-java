/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.password;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordTest.class);

    @Test
    public void testZeroizePassword() {
        Password password = new Password("test".toCharArray());
        // do not log senstive info
        // log.debug("password before clear: {}", password.toCharArray());
        password.clear();
        //log.debug("password after clear: {}", password.toCharArray());
        
    }
}
