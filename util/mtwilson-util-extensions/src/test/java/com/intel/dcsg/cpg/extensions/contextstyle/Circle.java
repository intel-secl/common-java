/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class Circle implements Shape {

    @Override
    public String getName() {
        return "circle";
    }

    @Override
    public String getColor() {
        return "black";
    }
    
    public int getRadius() {
        return 5;
    }
}
