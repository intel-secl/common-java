/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author jbuhacoff
 */
public class PropertiesTlsPolicyFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertiesTlsPolicyFactoryTest.class);

    @BeforeClass
    public static void registerExtensions() {
        WhiteboardExtensionProvider.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
    }
    
    @Test
    public void testCertificateDigestPolicy() {
        Properties p = new Properties();
        p.setProperty("mtwilson.api.tls.policy.certificate.sha1", "a5a776c164e1198b12340c452a2b4e8ce709d61f");
        TlsPolicy tlsPolicy = PropertiesTlsPolicyFactory.createTlsPolicy(p);
        log.debug("TlsPolicy class {}", tlsPolicy.getClass().getName());
    }

    @Test
    public void testCertificateDigestListPolicy() {
        String[] items = " a , b,c ,d, e ".split("\\s*,\\s*");
        assertEquals(5, items.length);
        for(String item: items) {
            log.debug("item '{}' with trim '{}'", item, item.trim());
        }
    }

    @Test
    public void testCertificateDigestListPolicy2() {
        String[] items = ", a ,, b,c, ,d, e ,".split("\\s*,\\s*");
        //assertEquals(5, items.length);
        for(String item: items) {
            log.debug("item '{}' with trim '{}'", item, item.trim());
        }
    }


}
