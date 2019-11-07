/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.security.http.jaxrs;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import javax.ws.rs.client.ClientRequestFilter;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import javax.ws.rs.Priorities;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Sample usage:
 * 
        clientConfig = new ClientConfig();
        clientConfig.register(new TokenAuthorizationFilter("token-value"));
 * 
 * Example http header added:
 * <pre>
 * Authorization: Token AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok
 * </pre>
 * 
 * @author jbuhacoff
 * @since 3.0
 */
@Priority(Priorities.AUTHORIZATION)
public class TokenAuthorizationFilter implements ClientRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenAuthorizationFilter.class);

    private final String headerName;
    private final String tokenType;
    private final String tokenValue;
    
    public TokenAuthorizationFilter(String tokenValue) {
        this("Token", tokenValue);
    }
    public TokenAuthorizationFilter(Password tokenValue) {
        this("Token", tokenValue);
    }
    public TokenAuthorizationFilter(String tokenType, String tokenValue) {
        this.headerName = "Authorization";
        this.tokenType = tokenType;
        this.tokenValue = tokenValue;
    }
    public TokenAuthorizationFilter(String tokenType, Password tokenValue) {
        this(tokenType, new String(tokenValue.toCharArray()));
    }
    public TokenAuthorizationFilter(String headerName, String tokenType, String tokenValue) {
        this.headerName = headerName;
        this.tokenType = tokenType;
        this.tokenValue = tokenValue;
    }

    
    /**
     * This method assumes that the entity body of the request is either null or a String or
     * has a toString() method that returns the String that should be signed.
     * 
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ClientRequestContext requestContext)
                        throws IOException { 
        // Modify the request
        try {
            String header = String.format("%s %s", tokenType, tokenValue);
            log.debug("header name '{}' value: {}", headerName, header);
            requestContext.getHeaders().add(headerName, header);
            
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }
    
}
