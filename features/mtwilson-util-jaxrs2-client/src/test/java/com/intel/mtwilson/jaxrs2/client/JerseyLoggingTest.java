/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JerseyLoggingTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JerseyLoggingTest.class);
    private static final String DEFAULT_LOGGER_CLASS_PATH = "org.glassfish.jersey.logging.LoggingFeature";

    
    private void setLogLevel(Class clazz, Level level) {
        Logger root = (Logger) LoggerFactory.getLogger(clazz);
        root.setLevel(level);
    }
    private void setLogLevel(String name, Level level) {
        Logger root = (Logger) LoggerFactory.getLogger(name);
        root.setLevel(level);
    }
    
    @Test
    public void testWithErrorLogging() throws MalformedURLException {
        setLogLevel(JaxrsClient.class, Level.ERROR);
        setLogLevel(DEFAULT_LOGGER_CLASS_PATH, Level.ERROR);
        JaxrsClient client = JaxrsClientBuilder.factory().url(new URL("https://intel.com")).proxy("proxy-us.intel.com", 911).tlsPolicy(new InsecureTlsPolicy()).build();
        log.debug("uri: {}", client.getTargetPath("/").getUri().toString());
        client.getTarget().request().get();
        /*
        Example output:
10:52:21.789 [main] DEBUG c.i.m.j.client.JerseyLoggingTest - uri: http://intel.com/
May 29, 2017 11:48:52 AM org.glassfish.jersey.logging.LoggingFeature log
INFO: 1 * LoggingFilter - Request received on thread main
1 > GET https://intel.com

11:48:52.275 [main] DEBUG c.i.m.j.c.ProxyConnectionFactory - ProxyConnectionFactory host:proxy-us.intel.com port:911 getConnection: https://intel.com
11:48:52.967 [main] WARN  c.i.d.c.t.p.i.AllowAllX509TrustManager - Insecure: accepting all server certificates with Socket
11:48:53.380 [main] WARN  c.i.d.c.t.p.i.AllowAllX509TrustManager - Insecure: accepting all server certificates with Socket
May 29, 2017 11:48:53 AM org.glassfish.jersey.filter.LoggingFilter log
INFO: 2 * LoggingFilter - Response received on thread main
2 < 200
2 < x-frame-options: sameorigin
2 < ETag: "cf34c658cd8d21:0"
2 < Date: Mon, 29 May 2017 18:48:53 GMT
2 < Content-Length: 99418
2 < Last-Modified: Mon, 29 May 2017 14:58:25 GMT
2 < Set-Cookie: src_countrycode=US; path=/; domain=.intel.com,detected_bandwidth=HIGH; path=/; domain=.intel.com
2 < Connection: keep-alive
2 < Accept-Ranges: bytes
2 < Content-Type: text/html
2 < Server: IA Web Server
2 < Cache-Control: max-age=1781
        
        */
    }

    @Test
    public void testWithInfoLogging() throws MalformedURLException {
        setLogLevel(JaxrsClient.class, Level.INFO);
        setLogLevel(DEFAULT_LOGGER_CLASS_PATH, Level.INFO);
        JaxrsClient client = JaxrsClientBuilder.factory().url(new URL("https://intel.com")).proxy("proxy-us.intel.com", 911).tlsPolicy(new InsecureTlsPolicy()).build();
        log.debug("uri: {}", client.getTargetPath("/").getUri().toString());
        client.getTarget().request().get();
        /*
        Example output:
11:50:01.791 [main] DEBUG c.i.m.j.client.JerseyLoggingTest - uri: https://intel.com/
May 29, 2017 11:50:02 AM org.glassfish.jersey.logging.LoggingFeature log
INFO: 1 * LoggingFilter - Request received on thread main
1 > GET https://intel.com

11:50:02.153 [main] DEBUG c.i.m.j.c.ProxyConnectionFactory - ProxyConnectionFactory host:proxy-us.intel.com port:911 getConnection: https://intel.com
11:50:02.615 [main] WARN  c.i.d.c.t.p.i.AllowAllX509TrustManager - Insecure: accepting all server certificates with Socket
11:50:02.864 [main] WARN  c.i.d.c.t.p.i.AllowAllX509TrustManager - Insecure: accepting all server certificates with Socket
May 29, 2017 11:50:03 AM org.glassfish.jersey.filter.LoggingFilter log
INFO: 2 * LoggingFilter - Response received on thread main
2 < 200
2 < x-frame-options: sameorigin
2 < ETag: W/"7afe718b35d8d21:0"
2 < Date: Mon, 29 May 2017 18:50:03 GMT
2 < Content-Length: 99418
2 < Last-Modified: Mon, 29 May 2017 04:39:23 GMT
2 < Set-Cookie: src_countrycode=US; path=/; domain=.intel.com,detected_bandwidth=HIGH; path=/; domain=.intel.com
2 < Connection: keep-alive
2 < Accept-Ranges: bytes
2 < Content-Type: text/html
2 < Server: IA Web Server
2 < Cache-Control: max-age=1689
        
        */
    }
    
    @Test
    public void testWithDebugLogging() throws MalformedURLException {
        setLogLevel(JaxrsClient.class, Level.DEBUG);
        setLogLevel(DEFAULT_LOGGER_CLASS_PATH, Level.DEBUG);
        JaxrsClient client = JaxrsClientBuilder.factory().url(new URL("https://intel.com")).proxy("proxy-us.intel.com", 911).tlsPolicy(new InsecureTlsPolicy()).build();
        log.debug("uri: {}", client.getTargetPath("/").getUri().toString());
        client.getTarget().request().get();
        // example output: http://intel.com/
    }

}
