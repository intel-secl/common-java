/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.keplerlake.authz.hmac;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Represents a key-value pair for a query string or http header parameter.
 *
 * @author jbuhacoff
 */
public class KeyValue {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final String key;
    private final String value;

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toQueryString() {
        return String.format("%s=%s", encode(key), encode(value));
    }

    /**
     * TODO: use an http parameter value encoder that complies with RFC 2616.
     *
     * @return
     */
    public String toParameterString() {
        return String.format("%s=\"%s\"", encode(key), encode(value)).replace("%2C", ",").replace("%3A", ":").replace("%2B", "+").replace("%3D", "=").replace("%2F", "/");
    }

    private static String encode(String text) {
        try {
            return URLEncoder.encode(text, UTF8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // java runtime guarantees availability of utf-8 so this will never happen
        }
    }

}
