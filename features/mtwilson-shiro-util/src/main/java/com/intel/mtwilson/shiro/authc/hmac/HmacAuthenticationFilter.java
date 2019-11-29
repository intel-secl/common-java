/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.hmac;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.http.Query;
import com.intel.keplerlake.authz.hmac.KeyValue;
import com.intel.keplerlake.authz.hmac.KeyValueList;
import com.intel.keplerlake.authz.hmac.Message;
import com.intel.keplerlake.authz.hmac.MessageBuilder;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.shiro.HttpAuthenticationFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class HmacAuthenticationFilter extends HttpAuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacAuthenticationFilter.class);
    private int expiresAfter = 60 * 60 * 1000; // 1 hour, in milliseconds, max is Integer.MAX_VALUE
    private final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);

    public HmacAuthenticationFilter() {
        super();
        setAuthenticationScheme("HMAC"); // to match output of HmacAuthorization class in the client
    }

    /**
     * Override the expiration window. Default is 1 hour.
     *
     * @param expiresAfter
     */
    public void setExpiresAfter(int expiresAfter) {
        this.expiresAfter = expiresAfter;
    }

    public int getExpiresAfter() {
        return expiresAfter;
    }

    /**
     * Given an http request object, this method re-creates the signed message
     * and creates an authentication token instance that has the "from"
     * username, the signed message, and the signature bytes. This "token" or
     * "credential" will then be used by the HmacCredentialsMatcher to verify
     * the signature using the server's secret key.
     *
     * @param request
     * @return
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        log.debug("createToken");
        try {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            KeyValueList authzParameters = parseAuthorization(httpRequest.getHeader("Authorization"));
            Map<String,String> authzParameterMap = toMap(authzParameters);

            // from, nonce, digestAlg, and signature will be populated from authz parameters
//            String from = null;
//            String realm = null;
//            String nonce = null;
//            String digestAlg = null;
//            Date timestamp = null;
//            boolean dateHeaderIncluded = false;
            String signatureBase64 = authzParameterMap.get("signature");

            MessageBuilder builder = MessageBuilder.factory();
            builder.httpMethod(httpRequest.getMethod());
            builder.uri(String.format("%s://%s:%d%s", httpRequest.getScheme(), httpRequest.getServerName(), httpRequest.getServerPort(), httpRequest.getRequestURI()));
            
            // look for header and query parameters
            for (KeyValue kv : authzParameters.list()) {
                String key = kv.getKey();
                if (!key.equals("signature")) {
                    // we add all parameters except for the "signature" parameter
                    builder.authzParam(kv.getKey(), kv.getValue());
                }
            }
            
            String headerNameCsv = authzParameterMap.get("headers");
            if( headerNameCsv != null ) {
                String[] headerNames = headerNameCsv.split(",");
                for (String headerName : headerNames) {
                    Enumeration<String> headerEnum = httpRequest.getHeaders(headerName);
                    if (headerEnum == null) {
                        builder.headerParam(headerName, "");
                    } else {
                        for (String value : Collections.list(headerEnum)) {
                            builder.headerParam(headerName, value);
                            /*
                            // in case timestamp parameter is missing we need to know if the date header is covered by the signature
                            if (headerName.equalsIgnoreCase("Date") && value != null && !value.isEmpty()) {
                                dateHeaderIncluded = true;
                            }
                            */
                        }
                    }
                }
            }
            
            String queryParameterNameCsv = authzParameterMap.get("query");
            if( queryParameterNameCsv != null ) {
                String queryString = httpRequest.getQueryString();
                if (queryString == null) {
                    queryString = "";
                }
                Map<String, List<String>> queryParams = Query.parse(queryString);
                String[] queryParameterNames = queryParameterNameCsv.split(",");
                for (String queryParameterName : queryParameterNames) {
                    List<String> values = queryParams.get(queryParameterName);
                    if (values != null) {
                        for (String value : values) {
                            builder.queryParam(queryParameterName, value);
                        }
                    }
                }
            }

            
            String from = authzParameterMap.get("from");
            String digestAlg = authzParameterMap.get("digest-alg");

            // if the request includes a body , get the digest
            if (httpRequest.getContentLength() > 0 && digestAlg != null) {
                byte[] content = getRequestBody(httpRequest);
                byte[] digest = Digest.algorithm(digestAlg).digest(content).getBytes();
                builder.digestBase64(Base64.encodeBase64String(digest));
            }

            Message message = builder.build();
            String messageText = message.toString();
            byte[] messageBytes = messageText.getBytes(Charset.forName("UTF-8"));
            
            HmacAuthenticationCredential hmacCredential = new HmacAuthenticationCredential(messageBytes, digestAlg, Base64.decodeBase64(signatureBase64));
            HmacAuthenticationToken hmacToken = new HmacAuthenticationToken(from, hmacCredential, request.getRemoteAddr());

            log.debug("createToken: returning HmacAuthenticationToken");
            return hmacToken;
        } catch (IOException e) {
            throw new AuthenticationException("Cannot authenticate request: " + e.getMessage(), e);
        }
    }

    private byte[] getRequestBody(HttpServletRequest httpRequest) throws IOException {
        log.debug("Reading request body");
        // get the request body (even if empty) - the input stream must be repeatable
        // so the endpoint will be able to read it again for processing the request
        InputStream in = httpRequest.getInputStream();
        if (!in.markSupported()) {
            throw new IOException("Request input stream is not repeatable; evaluating X509 authorization would prevent further processing of request");
        }
        byte[] requestBody = IOUtils.toByteArray(in);
        in.reset(); // to allow other filters or servlets to process the request
        return requestBody;
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest httpRequest, String[] headerNames) {
        HashMap<String, String> headerValues = new HashMap<>();
        for (String headerName : headerNames) {
            headerValues.put(headerName, httpRequest.getHeader(headerName));
        }
        return headerValues;
    }

    /**
     *
     * @param authorizationHeader
     * @return
     */
    private KeyValueList parseAuthorization(String authorizationHeader) {
        KeyValueList authzParameters = new KeyValueList();

        // splitting on spaces should yield "HMAC" followed by attribute name-value pairs
        String[] terms = authorizationHeader.split(" ");
        if (!"HMAC".equals(terms[0])) {
            throw new IllegalArgumentException("Authorization type is not HMAC");
        }
        for (int i = 1; i < terms.length; i++) {
            // each term after "PublicKey" is an attribute name-value pair, like realm="Example"
            Matcher attributeNameValue = headerAttributeNameValuePairPattern.matcher(terms[i]);
            if (attributeNameValue.find()) {
                String name = attributeNameValue.group(1);
                String value = attributeNameValue.group(2);
                authzParameters.add(name, value);
            }
        }
        return authzParameters;
    }

    /**
     * Assumes there is only one instance of any key;  if a key appears multiple
     * times in the list, only the last instance is used.
     * @param list
     * @return 
     */
    private Map<String,String> toMap(KeyValueList list) {
        HashMap<String,String> map = new HashMap<>();
        for(KeyValue kv : list.list()) {
            map.put(kv.getKey(), kv.getValue());
        }
        return map;
    }
}
