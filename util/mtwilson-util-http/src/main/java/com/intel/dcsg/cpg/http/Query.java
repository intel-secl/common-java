/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * There isn't a query string generator or parser class in the Java APIs.
 *
 * <ul>
 * <li>Spring framework has one:
 * org.springframework.web.util.UriComponentsBuilder (380KB)
 * <li>JAX-RS has one: javax.ws.rs.core.UriBuilder (120KB)
 * <li>Apache HttpClient has one: org.apache.http.client.utils.URIBuilder
 * (350KB)
 * </ul>
 *
 * @author jbuhacoff
 */
public class Query {

    private static final Charset UTF8 = Charset.forName("UTF-8"); // UTF-8 guaranteed to be available by java runtime, so UnsupportedEncodingException will not be thrown

    protected LinkedHashMap<String,ArrayList<String>> map;
    
    public Query() {
        map = new LinkedHashMap<>();
    }
    
    public Query(Map<String,String[]> map) {
        this();
        for(String key : map.keySet()) {
            ArrayList<String> copy = new ArrayList<>();
            copy.addAll(Arrays.asList(map.get(key)));
            this.map.put(key, copy);
        }
    }
    
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    public List<String> getAll(String name) {
        ArrayList<String> values = map.get(name);
        if( values == null ) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(values);
    }
    
    public String getFirst(String name) {
        List<String> values = getAll(name);
        if( values.isEmpty() ) {
            return null;
        }
        return values.get(0);
    }
    
    @Override
    public String toString() {
        ArrayList<String> pairs = new ArrayList<>();
        for(String key : map.keySet()) {
            List<String> values = map.get(key);
            if( values == null || values.isEmpty() ) { continue; }
            for(String value : values) {
                pairs.add(String.format("%s=%s", escape(key), escape(value)));
            }
        }
        return StringUtils.join(pairs, "&");
    }
    
    protected String escape(String text) {
        try {
            return URLEncoder.encode(text, UTF8.name());
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e); // java runtime guarantees availability of utf-8 so this will never happen
        }
    }
    
    public static Map<String, List<String>> parse(URL url) {
        return parse(url.getQuery());
    }

    /**
     *
     * @param query
     * @return map of String to List of String
     * method
     */
    public static Map<String, List<String>> parse(String query) {
        try {
            final Map<String, List<String>> parameters = new LinkedHashMap<>();
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int separator = pair.indexOf("=");
                final String key = separator > 0 ? URLDecoder.decode(pair.substring(0, separator), UTF8.name()) : pair;
                if (!parameters.containsKey(key)) {
                    parameters.put(key, new LinkedList<String>());
                }
                final String value = separator > 0 && pair.length() > separator + 1 ? URLDecoder.decode(pair.substring(separator + 1), UTF8.name()) : null;
                parameters.get(key).add(value);
            }
            return parameters;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // java runtime guarantees availability of utf-8 so this will never happen
        }
    }

    /**
     *
     * @param query
     * @return map of String to String ; if the query has multivalued
     * parameters, only the first one is used
     */
    public static Map<String, String> parseSinglevalued(String query) {
        HashMap<String, String> singlevaluedMap = new HashMap<>();
        Map<String, List<String>> multivaluedMap = parse(query);
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            List<String> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                singlevaluedMap.put(entry.getKey(), null);
            } else {
                singlevaluedMap.put(entry.getKey(), value.iterator().next());
            }
        }
        return singlevaluedMap;
    }
}
