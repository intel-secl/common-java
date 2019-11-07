/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
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
