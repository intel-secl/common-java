/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ModuleDeactivationException extends ContainerException {
    public ModuleDeactivationException() {
        super();
    }
    public ModuleDeactivationException(Throwable cause) {
        super(cause);
    }
    public ModuleDeactivationException(String message) {
        super(message);
    }
    public ModuleDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
