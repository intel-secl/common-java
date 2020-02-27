/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.keplerlake.authz.hmac.client;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.http.Query;
import com.intel.keplerlake.authz.hmac.AuthorizationHeaderBuilder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 *
 * Sample usage:
 *
 * clientConfig = new ClientConfig(); clientConfig.register(new
 * JaxrsHmacAuthorizationFilter("username", "password".toCharArray(),
 * "SHA256"));
 *
 * Example http header added: Authorization: HMAC digest-alg="SHA256",
 * from="client1234", headers="Content-Type,Content-Length,Origin",
 * nonce="AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok", query="x,y", realm="example.com",
 * signature="FP6D5KAoqvdNI5LhhnVZIbcczEXcDQyEaeor+L+bY7M=",
 * timestamp="2014-02-12T19:44:41-0800"
 *
 * Because this filter creates an Authorization header with a signature over the
 * http method, URL, and entity body (if provided), it should be the LAST filter
 * that is applied so that it can sign the final form of the entity body. The
 * only exception to that would be if a server filter decodes the entity body
 * BEFORE the security filter, for example gzip compression. In any such case,
 * you must take care to match the order in which the filters are applied on the
 * client and server.
 *
 * @author jbuhacoff
 */
@Priority(Priorities.AUTHORIZATION)
public class JaxrsHmacAuthorizationFilter implements ClientRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxrsHmacAuthorizationFilter.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final String digestAlg;
    private final String clientId;
    private final byte[] secretKeyBytes;

    /**
     * 
     * @param clientId a username or other identifier the server will recognize to retrieve the corresponding secret key for verifying the authorization header
     * @param secretKey secret key to use for generating the authorization header
     * @param digestAlg to use with HMAC, for example "SHA256"; the server must support use of this algorithm with HMAC
     */
    public JaxrsHmacAuthorizationFilter(String clientId, byte[] secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = secretKey;
        this.digestAlg = digestAlg;
    }

    /**
     * 
     * @param clientId a username or other identifier the server will recognize to retrieve the corresponding secret key for verifying the authorization header
     * @param secretKey secret key to use for generating the authorization header; will be encoded with UTF-8
     * @param digestAlg to use with HMAC, for example "SHA256"; the server must support use of this algorithm with HMAC
     */
    public JaxrsHmacAuthorizationFilter(String clientId, char[] secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = new String(secretKey).getBytes(Charset.forName("UTF-8"));
        this.digestAlg = digestAlg;
    }

    /**
     * 
     * @param clientId a username or other identifier the server will recognize to retrieve the corresponding secret key for verifying the authorization header
     * @param secretKey secret key to use for generating the authorization header; will be encoded with UTF-8
     * @param digestAlg to use with HMAC, for example "SHA256"; the server must support use of this algorithm with HMAC
     */
    public JaxrsHmacAuthorizationFilter(String clientId, Password secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = secretKey.toByteArray();
        this.digestAlg = digestAlg;
    }

    /**
     * This method assumes that the entity body of the request is either null or
     * a String or has a toString() method that returns the String that should
     * be signed.
     *
     * @param requestContext
     * @throws java.io.IOException
     */
    @Override
    public void filter(ClientRequestContext requestContext)
            throws IOException {
        // add the Authorization header to the request
        try {
            // initialize builder with the request, client id, secret key, digest algorithm, and content headers
            AuthorizationHeaderBuilder builder = AuthorizationHeaderBuilder.factory();

            builder.authzParam("from", clientId);
            builder.secretKey(secretKeyBytes);

            // add http method, uri, and digest of body (if present)
            String scheme = requestContext.getUri().getScheme();
            String port = "";
            if (requestContext.getUri().getPort() > -1) {
                port = ":" + String.valueOf(requestContext.getUri().getPort());
            }
            builder.httpMethod(requestContext.getMethod());
            builder.uri(String.format("%s://%s%s%s", scheme, requestContext.getUri().getHost(), port, requestContext.getUri().getPath()));
            builder.digestAlg(digestAlg);
            Object entity = requestContext.getEntity();
            if( entity != null ) {
                log.debug("requestContext.getEntityType(): {}", requestContext.getEntityType().toString());
                log.debug("requestContext.getEntityClass(): {}", requestContext.getEntityClass().getName());
                if (entity instanceof String) {
                    String entityString = (String) entity;
                    log.debug("requestContext.getEntityClass(): {}", entityString);
                    if (!entityString.isEmpty()) {
                        builder.digest(Digest.algorithm(digestAlg).digest(entityString.getBytes(UTF8)).getBytes());
                    }
                } else if (entity instanceof byte[]) {
                    byte[] entityBytes = (byte[]) entity;
                    log.debug("requestContext.getEntityClass(): {}", entityBytes);
                    if (entityBytes.length > 0) {
                        builder.digest(Digest.algorithm(digestAlg).digest(entityBytes).getBytes());
                    }
                } else {
                    log.debug("filter with request entity: {}", entity.toString());
                    byte[] content = entity.toString().getBytes(UTF8);
                    builder.digest(Digest.algorithm(digestAlg).digest(content).getBytes());
                }

            }
            
            builder.nonce();

            String requestDate = requestContext.getHeaderString("Date");
            if (requestDate == null) {
                builder.timestamp();
            } else {
                // try parsing in http date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                try {
                    builder.timestamp(dateFormat.parse(requestDate));
                } catch (ParseException e) {
                    log.error("Cannot parse date from http header: {}", requestDate, e);
                    log.debug("Using current date for timestamp");
                    builder.timestamp();
                }
            }

            // add client request headers
            if(!"GET".equalsIgnoreCase(requestContext.getMethod())){
                MultivaluedMap<String, String> headerMap = requestContext.getStringHeaders();
                String[] httpHeaderNames = new String[]{"Content-Type", "Content-Length"};
                for (String httpHeaderName : httpHeaderNames) {
                    List<String> headerValues = headerMap.get(httpHeaderName);
                    if (headerValues != null) {
                        for (String headerValue : headerValues) {
                            builder.headerParam(httpHeaderName, headerValue);
                        }
                    }
                }
            }

            // add query parameters
            String queryString = requestContext.getUri().getQuery();
            if (queryString != null) {
                Map<String, List<String>> queryParameterMap = Query.parse(queryString);
                for (String queryParameterName : queryParameterMap.keySet()) {
                    List<String> values = queryParameterMap.get(queryParameterName);
                    if (values != null) {
                        for (String value : values) {
                            builder.queryParam(queryParameterName, value);
                        }
                    }
                }
            }

            // format the message and compute the signature, and return the formatted authorization header
            String header = builder.build();
            log.debug("Authorization: {}", header);
            requestContext.getHeaders().add("Authorization", header);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new IOException(e);
        }

    }

}
