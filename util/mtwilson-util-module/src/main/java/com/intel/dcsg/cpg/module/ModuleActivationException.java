/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ModuleActivationException extends ContainerException {
    public ModuleActivationException() {
        super();
    }
    public ModuleActivationException(Throwable cause) {
        super(cause);
    }
    public ModuleActivationException(String message) {
        super(message);
    }
    public ModuleActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
