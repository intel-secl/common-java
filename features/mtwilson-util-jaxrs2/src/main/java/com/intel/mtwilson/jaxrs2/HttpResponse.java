/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.mtwilson.collection.MultivaluedHashMap;

/**
 * @since keplerlake
 * @author jbuhacoff
 */
public class HttpResponse {

    private Integer status = null;
    private final MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();

    public Integer getStatusCode() {
        return status;
    }

    public MultivaluedHashMap<String, String> getHeaders() {
        return headers;
    }

    public void setStatusCode(Integer statusCode) {
        this.status = statusCode;
    }
}
