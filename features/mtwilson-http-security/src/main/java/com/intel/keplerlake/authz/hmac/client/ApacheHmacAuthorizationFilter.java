/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.keplerlake.authz.hmac.client;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import javax.annotation.Priority;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.http.Query;
import com.intel.keplerlake.authz.hmac.AuthorizationHeaderBuilder;
import com.intel.mtwilson.security.http.apache.ApacheHttpAuthorization;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 *
 * Sample usage:
 *
 * <pre>
 * clientConfig = new ClientConfig(); clientConfig.register(new
 * ApacheHmacAuthorizationFilter("username", "password".toCharArray(),
 * "SHA256"));
 * </pre>
 *
 * Example http header added: 
 * 
 * <pre>
 * Authorization: HMAC digest-alg="SHA256",
 * from="client1234", headers="Content-Type,Content-Length,Origin",
 * nonce="AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok", query="x,y", realm="example.com",
 * signature="FP6D5KAoqvdNI5LhhnVZIbcczEXcDQyEaeor+L+bY7M=",
 * timestamp="2014-02-12T19:44:41-0800"
 * </pre>
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
public class ApacheHmacAuthorizationFilter implements ApacheHttpAuthorization {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApacheHmacAuthorizationFilter.class);

    private final String digestAlg;
    private final String clientId;
    private final byte[] secretKeyBytes;

    public ApacheHmacAuthorizationFilter(String clientId, byte[] secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = secretKey;
        this.digestAlg = digestAlg;
    }

    public ApacheHmacAuthorizationFilter(String clientId, char[] secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = new String(secretKey).getBytes(Charset.forName("UTF-8"));
        this.digestAlg = digestAlg;
    }

    public ApacheHmacAuthorizationFilter(String clientId, Password secretKey, String digestAlg) {
        this.clientId = clientId;
        this.secretKeyBytes = secretKey.toByteArray();
        this.digestAlg = digestAlg;
    }

    @Override
    public void addAuthorization(HttpRequest request) throws SignatureException {
        try {
            AuthorizationHeaderBuilder builder = prepare(request);
            String authorizationHeaderValue = builder.build();
            log.debug("Authorization: {}", authorizationHeaderValue);
            request.addHeader("Authorization", authorizationHeaderValue);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error("Cannot prepare authorization header", e);
            throw new SignatureException(e);
        }
    }

    /**
     * The entity must be repeatable. If the entity is null then an empty string
     * is used to represent it.
     *
     * @param request
     * @throws java.security.SignatureException
     */
    @Override
    public void addAuthorization(HttpEntityEnclosingRequest request) throws SignatureException {

        try {
            AuthorizationHeaderBuilder builder = prepare(request);

            // add the entity body, if present
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                if (!entity.isRepeatable()) {
                    throw new IllegalArgumentException("Cannot sign a non-repeatable request");
                }
                byte[] content = IOUtils.toByteArray(entity.getContent());
                if (content != null && content.length > 0) {
                    builder.digest(Digest.algorithm(digestAlg).digest(content).getBytes());
                }
            }

            String authorizationHeaderValue = builder.build();
            log.debug("Authorization: {}", authorizationHeaderValue);
            request.addHeader("Authorization", authorizationHeaderValue);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error("Cannot prepare authorization header", e);
            throw new SignatureException(e);
        }

    }

    private AuthorizationHeaderBuilder prepare(HttpRequest request) throws IOException {
        // initialize builder with the request, client id, secret key, digest algorithm, and content headers
        AuthorizationHeaderBuilder builder = AuthorizationHeaderBuilder.factory();

        builder.authzParam("from", clientId);
        builder.secretKey(secretKeyBytes);

        String uri = request.getRequestLine().getUri();
        URL url = new URL(uri);
        String scheme = url.getProtocol();
        String port = "";
        if (url.getPort() > -1) {
            port = ":" + String.valueOf(url.getPort());
        }
        builder.httpMethod(request.getRequestLine().getMethod());
        builder.uri(String.format("%s://%s%s%s", scheme, url.getHost(), port, url.getPath()));
        builder.digestAlg(digestAlg);

        builder.nonce();

        Header dateHeader = request.getFirstHeader("Date");
        if (dateHeader != null) {
            String requestDate = dateHeader.getValue();
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
        }

        // add client request headers
        Header[] headers = request.getAllHeaders();
        String[] httpHeaderNames = new String[]{"Content-Type", "Content-Length"};
        for (String httpHeaderName : httpHeaderNames) {
            for (Header header : headers) {
                if (header.getName().equalsIgnoreCase(httpHeaderName)) {
                    builder.headerParam(httpHeaderName, header.getValue());
                }
            }
        }

        // add query parameters
        String queryString = url.getQuery();
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

        return builder;

    }

}
