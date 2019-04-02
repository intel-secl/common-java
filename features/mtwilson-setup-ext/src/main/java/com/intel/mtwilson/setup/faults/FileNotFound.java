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
public class FileNotFound extends Fault {
    private String path;

    public FileNotFound(String path) {
        super(path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    
    
}
