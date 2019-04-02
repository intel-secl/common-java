/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TlsUtilTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testGetServerCertificates() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        URL url = new URL("https://192.168.0.1/sdk");
        X509Certificate[] certificates = TlsUtil.getServerCertificates(url);
        assertNotNull(certificates);
        assertTrue(certificates.length > 0);
        for(X509Certificate certificate : certificates) {
            log.debug("Certificate: {}", certificate.getSubjectX500Principal().getName());
        }
    }
}
