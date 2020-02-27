/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AbstractObjectWithMeta implements MetaDataAware {

    private final MetaData meta = new MetaData();
    
    @Override
    public MetaData getMeta() {
        return meta;
    }

}
