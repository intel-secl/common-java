/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.keplerlake.authz.hmac;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class AuthorizationHeaderBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthorizationHeaderBuilder.class);

    private final static Charset UTF8 = Charset.forName("UTF-8");
    private String httpMethod;
    private String uri;
    private String digestBase64;
    private String digestAlg;
    private Date timestamp;
    private byte[] nonce;
    private byte[] secretKey;
    private final KeyValueList httpHeaders = new KeyValueList();
    private final KeyValueList queryParameters = new KeyValueList();
    private final KeyValueList authzParameters = new KeyValueList();

    public static AuthorizationHeaderBuilder factory() {
        return new AuthorizationHeaderBuilder();
    }

    public AuthorizationHeaderBuilder digestAlg(String digestAlg) {
        this.digestAlg = digestAlg;
        return this;
    }

    public AuthorizationHeaderBuilder secretKey(byte[] secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public AuthorizationHeaderBuilder httpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public AuthorizationHeaderBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * 
     * @param digest may be null if there is no entity body
     * @return 
     */
    public AuthorizationHeaderBuilder digest(byte[] digest) {
        if( digest == null ) {
            this.digestBase64 = null;
        }
        else {
            this.digestBase64 = Base64.encodeBase64String(digest);
        }
        return this;
    }

    public AuthorizationHeaderBuilder queryParam(String key, String value) {
        queryParameters.add(key, value);
        return this;
    }

    public AuthorizationHeaderBuilder headerParam(String key, String value) {
        httpHeaders.add(key, value);
        return this;
    }

    public AuthorizationHeaderBuilder authzParam(String key, String value) {
        if (key.equalsIgnoreCase("digest-alg") || key.equalsIgnoreCase("nonce") || key.equalsIgnoreCase("timestamp") || key.equalsIgnoreCase("headers") || key.equalsIgnoreCase("query") || key.equalsIgnoreCase("signature")) {
            throw new IllegalArgumentException("Pre-defined authorization parameter must be added using existing methods");
        }
        authzParameters.add(key, value);
        return this;
    }

    /**
     * Generates a 24-byte nonce comprised of 8 bytes current time
     * (milliseconds) and 16 bytes random data.
     *
     * @return
     * @throws IOException if there was a problem generating the nonce
     */
    private byte[] generateNonce() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            long currentTime = System.currentTimeMillis();
            dos.writeLong(currentTime);

            SecureRandom r = new SecureRandom();
            byte[] randomBytes = new byte[16];
            r.nextBytes(randomBytes);
            dos.write(randomBytes);

            dos.flush();
        }
        return bos.toByteArray();
    }

    public AuthorizationHeaderBuilder nonce() throws IOException {
        nonce = generateNonce();
        return this;
    }

    public AuthorizationHeaderBuilder nonce(byte[] nonce) {
        this.nonce = nonce;
        return this;
    }

    public AuthorizationHeaderBuilder timestamp() {
        this.timestamp = new Date();
        return this;
    }

    public AuthorizationHeaderBuilder timestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Automatically generates nonce and uses current date as timestamp if they
     * were not already set.
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws MalformedURLException
     * @throws IOException
     */
    public String build() throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException, IOException {
        MessageBuilder messageBuilder = MessageBuilder.factory();

        if (nonce == null) {
            nonce = generateNonce();
        }
        String nonceBase64 = Base64.encodeBase64String(nonce);
        authzParameters.add("nonce", nonceBase64);

        if (timestamp == null) {
            timestamp = new Date();
        }
        String timestampIso8601 = Iso8601Date.format(timestamp);
        authzParameters.add("timestamp", timestampIso8601);

        // add all http headers and keep track of header names for the headers authz param
        ArrayList<String> httpHeaderNames = new ArrayList<>();
        for (KeyValue httpHeader : httpHeaders.list()) {
            messageBuilder.headerParam(httpHeader.getKey(), httpHeader.getValue());
            httpHeaderNames.add(httpHeader.getKey());
        }

        // add all query string parameters and keep track of parameter names for the query authz param
        ArrayList<String> queryParameterNames = new ArrayList<>();
        for (KeyValue queryParameter : queryParameters.list()) {
            messageBuilder.queryParam(queryParameter.getKey(), queryParameter.getValue());
            queryParameterNames.add(queryParameter.getKey());
        }

        authzParameters.add("digest-alg", digestAlg);

        // add http header names and query string parameter names to the
        // authorization parameters
        if (!httpHeaderNames.isEmpty()) {
            authzParameters.add("headers", StringUtils.join(httpHeaderNames, ","));
        }
        if (!queryParameterNames.isEmpty()) {
            authzParameters.add("query", StringUtils.join(queryParameterNames, ","));
        }

        // add http method, uri, and digest of body (if present)
        messageBuilder.httpMethod(httpMethod);
        messageBuilder.uri(uri);
        messageBuilder.digestBase64(digestBase64); // ok to pass null if body not present

        for (KeyValue kv : authzParameters.list()) {
            messageBuilder.authzParam(kv.getKey(), kv.getValue());
        }

        // format the message and compute the signature
        Message message = messageBuilder.build();
        String messageText = message.toString();
        log.debug("AuthorizationHeaderBuilder message: {}", messageText);
        log.debug("AuthorizationHeaderBuilder message base64: {}", Base64.encodeBase64String(messageText.getBytes(UTF8)));

        // signature must be added to http authorization header only after finalizing the message
        Signature signature = new Signature(digestAlg, secretKey);
        byte[] signatureBytes = signature.sign(messageText.getBytes(UTF8));
        String signatureBase64 = Base64.encodeBase64String(signatureBytes);
        log.debug("AuthorizationHeaderBuilder signature: {}", signatureBase64);
        authzParameters.add("signature", signatureBase64);

        // format the authorization header
        return String.format("HMAC %s", authzParameters.toParameterString());
    }

}
