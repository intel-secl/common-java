/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * XXX Tentative : considering removing the ModuleEvent interface and posting Objects directly, using
 * an expression language like XPath to evaluate their properties for filtering.
 * @author jbuhacoff
 */
public interface ModuleEvent {
    String getName();
}
