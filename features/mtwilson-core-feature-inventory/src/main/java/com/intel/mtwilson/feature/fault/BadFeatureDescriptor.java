/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.feature.fault;

import com.intel.dcsg.cpg.validation.Fault;

/**
 * When the "feature.xml" file cannot be read
 * @author jbuhacoff
 */
public class BadFeatureDescriptor extends Fault {
    private String featureId;
    public BadFeatureDescriptor(String featureId) {
        super(featureId);
    }
    public BadFeatureDescriptor(Throwable cause, String featureId) {
        super(cause, featureId);
    }

    public String getFeatureId() {
        return featureId;
    }
    
}
