/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class TestAllCapsEnvironmentConfiguration {
    @Test
    public void testAllCapsTranformation() {
        CommonsAllCapsEnvironmentConfiguration conf = new CommonsAllCapsEnvironmentConfiguration();
        assertEquals("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE", conf.toAllCaps("mtwilson.api.ssl.requireTrustedCertificate"));
        assertEquals("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE", conf.toAllCaps("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE"));
        assertEquals("MTWILSON_API_SSL_VERIFY_HOSTNAME", conf.toAllCaps("mtwilson.api.ssl.verifyHostname"));
        assertEquals("MTWILSON_API_SSL_VERIFY_HOSTNAME", conf.toAllCaps("mtwilson.api.SSL_verifyHostname"));
    }
}
