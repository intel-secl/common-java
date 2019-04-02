/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author jbuhacoff
 */
public class RpcLocator implements Locator<Rpc> {
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(Rpc item) {
        item.setId(id);
    }
    
}
