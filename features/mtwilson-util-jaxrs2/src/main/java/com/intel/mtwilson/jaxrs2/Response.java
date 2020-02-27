/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @since keplerlake
 * @author jbuhacoff
 * @param <T>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response<T extends MetaDataAware> implements Faults, MetaDataAware {
    private T content;
    private final MetaData meta = new MetaData();

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public MetaData getMeta() {
        return meta;
    }
    

    
    private final Collection<Fault> faults = new ArrayList<>();

    @Override
    public Collection<Fault> getFaults() {
        return faults;
    }

    
    /**
     * The httpResponse member does NOT get serialized in responses, it exists
     * so that a "business layer" object can provide hints on the content type
     * and status of the response without preempting other processing or losing
     * other response information by throwing an exception
     */
    @JsonIgnore
    private final HttpResponse httpResponse = new HttpResponse();

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

}
