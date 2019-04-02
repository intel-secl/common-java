/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * This event is posted after a module is activated.
 * @author jbuhacoff
 */
public class ModuleActivationEvent implements ModuleEvent {
    // XXX tentative
    public static final String MODULE_ACTIVATION_EVENT = "module-activation-event";

    public ModuleActivationEvent(Module module, Object component) {
        
    }

    // XXX tentative
    @Override
    public String getName() {
        return MODULE_ACTIVATION_EVENT;
    }
}
