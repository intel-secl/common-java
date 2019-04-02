/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface ModuleRepository {
    List<Module> listModules();
    boolean contains(String artifact);
}
