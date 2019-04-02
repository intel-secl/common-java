/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentActivationException extends ContainerException {
    public ComponentActivationException() {
        super();
    }
    public ComponentActivationException(Throwable cause) {
        super(cause);
    }
    public ComponentActivationException(String message) {
        super(message);
    }
    public ComponentActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
