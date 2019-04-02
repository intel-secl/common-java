/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions;

/**
 *
 * @author jbuhacoff
 */
public interface Registrar {
    /**
     * Given a class, decides if it should be registered as a plugin for 
     * one or more interfaces, and registers it as necessary.
     * 
     * @param clazz
     * @return true if the class was registered, false if it was skipped
     */
    boolean accept(Class<?> clazz);
}
