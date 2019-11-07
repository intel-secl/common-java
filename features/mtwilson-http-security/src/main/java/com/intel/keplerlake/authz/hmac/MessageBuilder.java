/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.keplerlake.authz.hmac;

import com.intel.keplerlake.authz.hmac.Message.Section;

/**
 * This class provides a convenient way to construct the message bytes to be
 * passed to the HMAC function when creating or verifying a request.
 *
 * @author jbuhacoff
 */
public class MessageBuilder {
    private final Message message = new Message();

    public static MessageBuilder factory() {
        return new MessageBuilder();
    }
    
    public MessageBuilder digestBase64(String digestBase64) {
        message.digestBase64 = digestBase64;
        return this;
    }

    public MessageBuilder httpMethod(String httpMethod) {
        message.httpMethod = httpMethod;
        return this;
    }

    public MessageBuilder uri(String uri) {
        message.uri = uri;
        return this;
    }

    public MessageBuilder authzParam(String key, String value) {
        message.authorizationParameters.add(new KeyValue(key, value));
        return this;
    }

    public MessageBuilder queryParam(String key, String value) {
        message.query.add(new KeyValue(key, value));
        return this;
    }

    public MessageBuilder headerParam(String key, String value) {
        message.headers.add(new KeyValue(key, value));
        return this;
    }

    /**
     * This method only needed to extend the message with a section that both
     * client and server support. Note that you cannot add one of the predefined
     * sections with this method.
     *
     * @param section
     * @return
     */
    public MessageBuilder section(Section section) {
        if (section.name.equalsIgnoreCase("HTTP")
                || section.name.equalsIgnoreCase("URI")
                || section.name.equalsIgnoreCase("AUTHORIZATION")
                || section.name.equalsIgnoreCase("QUERY")
                || section.name.equalsIgnoreCase("REQUEST")
                || section.name.equalsIgnoreCase("DIGEST")) {
            throw new IllegalArgumentException("Pre-defined section must be populated with other methods");
        }
        message.extensions.add(section);
        return this;
    }

    public Message build() {
        return message;
    }
}
