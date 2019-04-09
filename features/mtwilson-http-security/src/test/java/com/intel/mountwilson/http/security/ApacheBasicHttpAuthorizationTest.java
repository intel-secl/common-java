/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mountwilson.http.security;

import com.intel.mtwilson.security.http.apache.ApacheBasicHttpAuthorization;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ApacheBasicHttpAuthorizationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApacheBasicHttpAuthorizationTest.class);
    
    @Test
    public void testBasicLogin() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials("Aladdin","open sesame");
        ApacheBasicHttpAuthorization a = new ApacheBasicHttpAuthorization(c);
        HttpGet get = new HttpGet();
        a.addAuthorization(get);
        assertEquals("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", get.getHeaders("Authorization")[0].getValue());
        log.debug("Authorization: {}", get.getHeaders("Authorization")[0]);
    }
}
