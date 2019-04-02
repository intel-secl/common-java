/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

import com.intel.dcsg.cpg.extensions.Plugin;

/**
 *
 * @author jbuhacoff
 */
@Plugin
public class Square implements Shape {

    @Override
    public String getName() {
        return "square";
    }

    @Override
    public String getColor() {
        return "red";
    }
    
    public int getLength() {
        return 7;
    }
    
}
