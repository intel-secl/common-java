/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.mtwilson.retry.Backoff;
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
    private final Backoff backoff;
    
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
        this.backoff = null;
    }

    public JaxrsClient(Client client, WebTarget target, Backoff backoff) {
        this.client = client;
        this.target = target;
        this.backoff = backoff;
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
        this.backoff = jaxrsClient.getBackoff();
    }
    
    public Client getClient() {
        return client;

    }

    public WebTarget getTarget() {
        return target;
    }

    public Backoff getBackoff() {
        return backoff;
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
                Object value = queryParam.getValue();
                if( value instanceof Map ) {
                    // copy each key-value of the map to the target as a query param
                    // we use the param name as the prefix, so "" is a valid param name for
                    // a map because it means there isn't a prefix
                    String prefix = queryParam.getKey();
                    Map<String,Object> valueMap = (Map)value;
                    for( Map.Entry<String, Object> mapQueryParam : valueMap.entrySet() ) {
                        target = target.queryParam(prefix + mapQueryParam.getKey(), mapQueryParam.getValue());
                    }
                }
                else {
                    target = target.queryParam(queryParam.getKey(), queryParam.getValue());
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot generate query parameters", e);
        }
        return target;
    }
    
}
