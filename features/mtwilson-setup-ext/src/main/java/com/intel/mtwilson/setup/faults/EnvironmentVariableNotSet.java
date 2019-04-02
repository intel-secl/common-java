/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.faults;

import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class EnvironmentVariableNotSet extends Fault {
    private String name;

    public EnvironmentVariableNotSet(String name) {
        super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    
}
