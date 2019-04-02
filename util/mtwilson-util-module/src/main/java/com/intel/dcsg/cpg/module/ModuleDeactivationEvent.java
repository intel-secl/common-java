/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * This event is posted before a module is deactivated, to allow consumers to release resources before
 * they become invalid.
 * @author jbuhacoff
 */
public class ModuleDeactivationEvent implements ModuleEvent {
    // XXX tentative
    public static final String MODULE_DEACTIVATION_EVENT = "module-deactivation-event";

    public ModuleDeactivationEvent(Module module, Object component) {
        
    }

    // XXX tentative
    @Override
    public String getName() {
        return MODULE_DEACTIVATION_EVENT;
    }
}
