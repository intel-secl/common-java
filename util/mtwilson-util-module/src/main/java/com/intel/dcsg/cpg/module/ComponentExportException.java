/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentExportException extends ContainerException {
    public ComponentExportException() {
        super();
    }
    public ComponentExportException(Throwable cause) {
        super(cause);
    }
    public ComponentExportException(String message) {
        super(message);
    }
    public ComponentExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
