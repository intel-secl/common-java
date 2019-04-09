/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

/**
 *
 * @author jbuhacoff
 */
public class ProxyConnectionFactory implements HttpUrlConnectorProvider.ConnectionFactory{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyConnectionFactory.class);
    private String proxyHost;
    private int proxyPort;
    
    public ProxyConnectionFactory(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        log.debug("ProxyConnectionFactory host:{} port:{} getConnection: {}",proxyHost,proxyPort,url.toExternalForm());
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return (HttpURLConnection)url.openConnection(proxy);
    }
    
}
