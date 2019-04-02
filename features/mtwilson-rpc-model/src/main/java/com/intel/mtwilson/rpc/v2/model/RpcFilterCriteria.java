/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class RpcFilterCriteria implements FilterCriteria<Rpc> {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    
    @QueryParam("statusEqualTo")
    public Rpc.Status status;
    
}
