/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.io.UUID;

/**
 * Minimum requirement for a "document" is to have an id attribute which is
 * a UUID. We use UUID only because it makes it easy for generic processing
 * such as creating a new UUID when needed, and always having a way to reference
 * a specific document that is likely to be unique (especially if the context
 * is restricted). 
 * This abstract class also contains an Attributes field so that new attributes can be
 * passed through existing code without breaking things. For example if a new
 * backend feature is added which must be enabled by passing a new option,
 * subclasses of AbstractDocument could accept that new option as input without
 * expecting it at authoring time. 
 * 
 * @author jbuhacoff
 */
public abstract class AbstractDocument extends AbstractObjectWithMeta implements Extensions {
    private UUID id;
    private String createdDate;
    
    @JsonUnwrapped
    private final Attributes extensions = new Attributes();

    public AbstractDocument() {
    }
    
    
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Attributes getExtensions() {
        return extensions;
    }

    
}
