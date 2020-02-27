/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Any object can include a MetaObject property to keep track of related
 * information that is required by the application.
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MetaData implements Realm, Id, Type, Links, Faults, Extensions, Operation, Status {
    private String realm;
    private String id;
    private String type;
    private String operation;
    private String status;
    private URL href;
    
    /**
     * Holds links to relevant information, such as a
     * rel:created link for created content, or rel:registered link for registered
     * keys.
     */
    private final Map<String,Link> links = new HashMap<>();
    private final Collection<Fault> faults = new ArrayList<>();
    
    @JsonUnwrapped
    private final Attributes extensions = new Attributes();

    @Override
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public URL getHref() {
        return href;
    }

    public void setHref(URL href) {
        this.href = href;
    }

    
    @JsonProperty("link")
    @Override
    public Map<String,Link> getLinkMap() {
        return links;
    }

    @Override
    public Collection<Fault> getFaults() {
        return faults;
    }

    @Override
    public Attributes getExtensions() {
        return extensions;
    }

    @Override
    public Link getLink(String relation) {
        return links.get(relation);
    }

}
