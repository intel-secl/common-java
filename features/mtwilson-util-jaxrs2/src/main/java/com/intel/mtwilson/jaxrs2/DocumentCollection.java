/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The "links" on a collection could be to prev/next pages in a paginated collection
 * The "linked" section is for pre-loading documents; for example a subclass 
 * called HostCollection would have a List<Host> getHosts() method and each host
 * might refer to an MLE by ID and the complete MLE object would be included in
 * the collection's "linked" section so the client doesn't need to make an
 * additional request for every reference - most or all can be pre-loaded and
 * provided in the "linked" section.
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public abstract class DocumentCollection<T> implements Faults {
    private final HashMap<String,Object> meta = new HashMap<String,Object>();
    private final HashMap<String,Object> links = new HashMap<String,Object>();
    private final HashMap<String,Object> linked = new HashMap<String,Object>();
    private final List<Fault> faults = new ArrayList<>();

    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, Object> getLinks() {
        return links;
    }
    
    public Map<String, Object> getLinked() {
        return linked;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore // jackson 2.x
    public abstract List<T> getDocuments();

    @Override
    public List<Fault> getFaults() {
        return faults;
    }


}
