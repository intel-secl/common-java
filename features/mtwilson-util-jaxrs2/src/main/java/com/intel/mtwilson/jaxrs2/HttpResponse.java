/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
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
