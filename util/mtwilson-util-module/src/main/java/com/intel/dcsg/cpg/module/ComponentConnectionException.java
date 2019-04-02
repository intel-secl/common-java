/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentConnectionException extends ContainerException {
    public ComponentConnectionException() {
        super();
    }
    public ComponentConnectionException(Throwable cause) {
        super(cause);
    }
    public ComponentConnectionException(String message) {
        super(message);
    }
    public ComponentConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
