/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * @author jbuhacoff
 */
public class ContentSecurityPolicyFilter implements ContainerResponseFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentSecurityPolicyFilter.class);

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        String host = request.getUriInfo().getRequestUri().getHost();
//        int port = request.getUriInfo().getRequestUri().getPort();
        String contentSecurityPolicy = "default-src {host}; form-action {host}; frame-ancestors {host}; plugin-types application/pdf".replace("{host}", host);
        log.debug("Content-Security-Policy: {}", contentSecurityPolicy);
//            response.getStringHeaders().add("Incident-Tag", incidentTag); // causes exception: java.lang.UnsupportedOperationException  thrown by the string headers map 
        response.getHeaders().add("Content-Security-Policy", contentSecurityPolicy);
    }

}
