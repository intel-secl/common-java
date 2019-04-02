/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file.model;

/**
 *
 * @author jbuhacoff
 */
public class PermissionFormatException extends IllegalArgumentException {

    public PermissionFormatException() {
        super();
    }

    public PermissionFormatException(String s) {
        super(s);
    }

    public PermissionFormatException(Throwable cause) {
        super(cause);
    }

    public PermissionFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
