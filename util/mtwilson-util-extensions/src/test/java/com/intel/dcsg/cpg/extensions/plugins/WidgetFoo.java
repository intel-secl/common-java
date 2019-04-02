/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.plugins;

/**
 *
 * @author jbuhacoff
 */
public class WidgetFoo implements Widget {
    
    @Override
    public String getType() {
        return "foo";
    }

    @Override
    public void run() {
        System.out.println("WidgetFoo");
    }
    
}
