/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyAwareConnectionFactory implements HttpUrlConnectorProvider.ConnectionFactory{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyAwareConnectionFactory.class);
    private TlsPolicy tlsPolicy;
    
    public TlsPolicyAwareConnectionFactory(TlsPolicy tlsPolicy) {
        this.tlsPolicy = tlsPolicy;
    }
    
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        log.debug("TlsPolicyAwareConnectionFactory getConnection: {}", url.toExternalForm());
        TlsConnection tlsConnection = new TlsConnection(url, tlsPolicy);
        return tlsConnection.openConnection();
    }
    
}
