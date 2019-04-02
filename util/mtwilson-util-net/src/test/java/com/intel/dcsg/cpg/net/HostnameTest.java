/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.net;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HostnameTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostnameTest.class);

    @Test
    public void testValidHostname() {
        assertTrue(ValidationUtil.isValidWithRegex("novamgmt21.mgmt", RegexPatterns.FQDN));
    }
}
