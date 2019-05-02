package com.intel.mtwilson.security.http;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ArbitraryHostRequestFilter implements ContainerRequestFilter {

    private static Logger log = LoggerFactory.getLogger(ArbitraryHostRequestFilter.class);
    
    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext containerRequest)
            throws WebApplicationException {
            
        String hName = request.getServerName();
        String lName = request.getLocalName();
        String hIP = null;
        try {
            hIP = InetAddress.getByName(hName).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to get web server address");
        }
        if (!lName.equals(hIP)) {
            containerRequest.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Invalid Request")
                    .build());
            log.error("Invalid host header, dropping request from {}", request.getRemoteHost()); 
            return;         
        }
        
    }
}
