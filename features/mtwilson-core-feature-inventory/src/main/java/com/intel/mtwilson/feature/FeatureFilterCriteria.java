/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.feature;

import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class FeatureFilterCriteria {
    @QueryParam("id")
    public String featureId;
    
    @QueryParam("extends")
    public String featureExtends;
}
