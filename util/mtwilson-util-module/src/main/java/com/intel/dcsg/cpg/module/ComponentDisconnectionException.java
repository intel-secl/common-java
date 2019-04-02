/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentDisconnectionException extends ContainerException {
    public ComponentDisconnectionException() {
        super();
    }
    public ComponentDisconnectionException(Throwable cause) {
        super(cause);
    }
    public ComponentDisconnectionException(String message) {
        super(message);
    }
    public ComponentDisconnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
