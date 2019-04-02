/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentDeactivationException extends ContainerException {
    public ComponentDeactivationException() {
        super();
    }
    public ComponentDeactivationException(Throwable cause) {
        super(cause);
    }
    public ComponentDeactivationException(String message) {
        super(message);
    }
    public ComponentDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
