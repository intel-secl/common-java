/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentNotificationException extends ContainerException {
    public ComponentNotificationException() {
        super();
    }
    public ComponentNotificationException(Throwable cause) {
        super(cause);
    }
    public ComponentNotificationException(String message) {
        super(message);
    }
    public ComponentNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
