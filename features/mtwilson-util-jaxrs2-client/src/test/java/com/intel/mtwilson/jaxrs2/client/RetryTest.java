/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.mtwilson.retry.ConstantBackoff;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * NOTE: the private methods annotated with @RetryOnException are simulating
 * the application logic, wherein any work that involves a network resource
 * that might need a retry capability is wrapped in a function that is
 * annotated so the retry will happen without any other boilerplate code. 
 * 
 * NOTE: after changing methods that use aspectj, if you don't see
 * expected result do a clean build and then run the test method
 * 
 * @author jbuhacoff
 */
public class RetryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryTest.class);
    
    /**
     * Expected failure after making one attempt to connect
     * 
     * @throws IOException 
     */
    @Test(expected=Exception.class)
    public void testConnectExceptionOnce() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "http://localhost:9999");
        JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).build();

        String response = client.getTarget()
                .path("path/to/resource")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        log.debug("response: {}", response);
    }
    
    /**
     * we expect to throw the exception but after RETRY 3 times (total of 4 attempts)
     * @throws IOException 
     */
    @Test(expected=Exception.class)
    public void testConnectExceptionRetry() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "http://localhost:9999");
        JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).retry(new ConstantBackoff(5), 3).build();

        String response = client.getTarget()
                .path("path/to/resource")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        log.debug("response: {}", response);
    }

    /**
     * we expect to throw the exception but after RETRY 3 times (total of 4 attempts)
     * @throws IOException 
     */
    @Test(expected=Exception.class)
    public void testConnectExceptionRetryFromConfiguration() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "http://localhost:9999");
        properties.setProperty("retry.max", "3");
        properties.setProperty("retry.backoff.constant", "5");
        JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).build();

        String response = client.getTarget()
                .path("path/to/resource")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        log.debug("response: {}", response);
    }
    
    /**
     * @throws IOException 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testConnectExceptionRetryIllegal() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "http://localhost:9999");
        JaxrsClientBuilder.factory().configuration(properties).retry(null, -1).build();
        fail(); // shouldn't get here
    }
    
    
}
