/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.keplerlake.authz.hmac;

import com.intel.dcsg.cpg.http.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates the message to be signed. Used by the MessageBuilder to store
 * the data prior to serialization. Can also parse an existing
 *
 * @author jbuhacoff
 */
public class Message {

    protected final ArrayList<Section> extensions = new ArrayList<>();

    // HTTP
    protected String httpMethod = null;
    // URI
    protected String uri = null;

    /**
     * Required: digest-alg, nonce, timestamp
     *
     * Optional: from, realm, headers, query.
     *
     * Prohibited: signature (it cannot be part of the message)
     */
    protected final KeyValueList authorizationParameters = new KeyValueList();

    // QUERY
    protected final KeyValueList query = new KeyValueList();

    // HEADERS
    protected final KeyValueList headers = new KeyValueList();

    // DIGEST
    protected String digestBase64 = null;

    public Message() {
    }

    public KeyValueList getAuthorizationParameters() {
        return authorizationParameters;
    }

    public String getDigestBase64() {
        return digestBase64;
    }

    public KeyValueList getHeaders() {
        return headers;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public KeyValueList getQuery() {
        return query;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        ArrayList<Section> sections = new ArrayList<>();
        sections.add(new Section("HTTP", httpMethod));
        sections.add(new Section("URI", uri));
        sections.add(new Section("AUTHORIZATION", authorizationParameters.toQueryString()));
        sections.add(new Section("QUERY", query.toQueryString()));
        sections.add(new Section("REQUEST", headers.toQueryString()));
        if( digestBase64 != null ) {
            sections.add(new Section("DIGEST", digestBase64));
        }
        sections.addAll(extensions);
        Collections.sort(sections, new SectionComparator());
        StringBuilder builder = new StringBuilder();
        for (Section section : sections) {
            builder.append(section.toString());
        }
        return builder.toString();
    }

    public static Message parse(String messageText) {
        Message message = new Message();
        // each section is a line like "{name}: {value}\n" 
        String[] lines = StringUtils.split(messageText, '\n');
        for (String line : lines) {
            String[] keyValue = StringUtils.splitByWholeSeparator(line, ": ");
            switch (keyValue[0]) {
                case "HTTP":
                    message.httpMethod = keyValue[1];
                    break;
                case "URI":
                    message.uri = keyValue[1];
                    break;
                case "DIGEST":
                    message.digestBase64 = keyValue[1];
                    break;
                case "AUTHORIZATION":
                    Map<String, String> authorizationMap = Query.parseSinglevalued(keyValue[1]);
                    for (Map.Entry<String, String> entry : authorizationMap.entrySet()) {
                        message.authorizationParameters.add(entry.getKey(), entry.getValue());
                    }
                    break;
                case "QUERY":
                    Map<String, String> queryMp = Query.parseSinglevalued(keyValue[1]);
                    for (Map.Entry<String, String> entry : queryMp.entrySet()) {
                        message.query.add(entry.getKey(), entry.getValue());
                    }
                    break;
                case "REQUEST":
                    Map<String, String> headerMap = Query.parseSinglevalued(keyValue[1]);
                    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                        message.headers.add(entry.getKey(), entry.getValue());
                    }
                    break;
                default:
                    message.extensions.add(new Section(keyValue[0], keyValue[1]));
                    break;
            }
        }
        return message;
    }

    /**
     * Represent a section of the message: HTTP, URI, AUTHORIZATION, QUERY,
     * REQUEST, DIGEST. If the content is null or empty, the toString method
     * will return an empty string.
     */
    public static class Section {

        protected final String name;
        protected final String content;

        public Section(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String toString() {
            if (content == null || content.isEmpty()) {
                return "";
            }
            return String.format("%s: %s\n", name, content);
        }

    }

    /**
     * Helper class to sort sections by name.
     */
    public static class SectionComparator implements Comparator<Section> {

        @Override
        public int compare(Section o1, Section o2) {
            return o1.name.compareTo(o2.name);
        }
    }
}
