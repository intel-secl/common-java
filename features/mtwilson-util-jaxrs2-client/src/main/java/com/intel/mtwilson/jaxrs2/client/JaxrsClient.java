/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author jbuhacoff
 */
public class JaxrsClient {
    private Client client;
    private WebTarget target;
    
    /**
     * Creates a client using an existing configured JAX-RS client and a
     * specified web target.
     * 
     * @param client
     * @param target 
     */
    public JaxrsClient(Client client, WebTarget target) {
        this.client = client;
        this.target = target;
    }
    
    /**
     * Creates a new client instance using an existing configured client and
     * web target combination.
     * 
     * @param jaxrsClient 
     */
    public JaxrsClient(JaxrsClient jaxrsClient) {
        this.client = jaxrsClient.getClient();
        this.target = jaxrsClient.getTarget();
    }
    
    public Client getClient() {
        return client;

    }

    public WebTarget getTarget() {
        return target;
    }

    public WebTarget getTargetPath(String path) {
        return target.path(path);
    }
    
    public WebTarget getTargetWithQueryParams(Object bean) {
        return addQueryParams(getTarget(), bean);
    }

    public WebTarget getTargetPathWithQueryParams(String path, Object bean) {
        return addQueryParams(getTarget().path(path), bean);
    }
    
    public static WebTarget addQueryParams(WebTarget target, Object bean) {
        try {
            Map<String, Object> properties = ReflectionUtil.getQueryParams(bean);
            for (Map.Entry<String, Object> queryParam : properties.entrySet()) {
                if (queryParam.getValue() == null) {
                    continue;
                }
                target = target.queryParam(queryParam.getKey(), queryParam.getValue());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot generate query parameters", e);
        }
        return target;
    }
    
}
