/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class Triangle implements Shape {

    @Override
    public String getName() {
        return "triangle";
    }

    @Override
    public String getColor() {
        return "green";
    }
    
    public int[] getSides() {
        return new int[] { 3, 4, 5 };
    }
}
