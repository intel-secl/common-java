/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.annotation;

/**
 * Not annotated directly, so will not count as a plugin for @Fruit
 * @author jbuhacoff
 */
public class GreenApple extends Apple {

    @Override
    public String toString() {
        return "green apple";
    }

}
