/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import java.util.Properties;
import org.junit.Test;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import javax.ws.rs.core.MediaType;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class CheckTlsTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckTlsTest.class);

    @BeforeClass
    public static void setupTlsPolicy() {
        WhiteboardExtensionProvider.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
    }
    
    @Test
    public void testTlsConnection() {
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "https://10.1.69.137:9449");
        properties.setProperty("tls.policy.certificate.sha256", "524197ac887b045d9de5400a914bd20039f9a598910cfa53d8ba333a17b2f42a");
         JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).build();
         log.debug("response; {}", client.getTargetPath("/cgi-bin/fileList.sh").request(MediaType.TEXT_PLAIN_TYPE).get().toString());
    }
    
}
