/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.server.filter;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * @author jbuhacoff
 */
public class ErrorLogFilter implements ContainerResponseFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorLogFilter.class);

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        Response.Status.Family httpStatusFamily = Response.Status.Family.familyOf(response.getStatus());
        if( httpStatusFamily == Response.Status.Family.CLIENT_ERROR || httpStatusFamily == Response.Status.Family.SERVER_ERROR ) {
            String incidentTag = RandomUtil.randomHexString(4); // 4 bytes => 8 hex digits  
            log.debug("Incident Tag #{}", incidentTag);
            response.getHeaders().add("Incident-Tag", incidentTag);
            if (log.isDebugEnabled()) {
                log.debug("Request method: {}", request.getMethod());
                log.debug("Request URI: {}", request.getUriInfo().getRequestUri().toString());
                // request headers
                MultivaluedMap<String, String> requestHeaderMap = request.getHeaders();
                for (String headerName : requestHeaderMap.keySet()) {
                    List<String> headerValueList = requestHeaderMap.get(headerName);
                    if(!headerName.equalsIgnoreCase("Authorization")){
                    log.debug("Request header name {} value {}", headerName, headerValueList);
                    }
                }
                // request body
                try {
                    InputStream requestInput = request.getEntityStream(); // throws IllegalStateException if the stream is already closed and is not repeatable
                    MediaType requestMediaType = request.getMediaType();
                    log.debug("Request media type: {}", requestMediaType);
                    if (requestMediaType != null && requestInput != null) {
                        String mediaTypeFormat = String.format("%s/%s", request.getMediaType().getType(), request.getMediaType().getSubtype());
                        switch (mediaTypeFormat) {
                            case "text/plain":
                            case "application/plaintext":
                            case "application/json":
                            case "application/ld+json":
                            case "application/xml":
                            case "application/x-pem-file":
                            case "message/rfc822":
                            case "application/samlassertion+xml":
                                String content = IOUtils.toString(requestInput, Charset.forName("UTF-8"));
                                log.debug("Request content: {}", content);
                                break;
                            default:
                                log.debug("Request content base64-encoded: {}", Base64.encodeBase64String(IOUtils.toByteArray(requestInput)));
                                break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Request content not available: {}", e.toString());
                }
                // response status code
                log.debug("Response status: {} {}", response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
                // response headers
                MultivaluedMap<String, Object> responseHeaderMap = response.getHeaders();
                for (String headerName : responseHeaderMap.keySet()) {
                    List<Object> headerValueList = responseHeaderMap.get(headerName);
                    log.debug("Response header name {} value {}", headerName, headerValueList);
                }
            }
        }
    }
    
}
