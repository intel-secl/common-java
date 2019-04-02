/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

/**
 *
 * @author jbuhacoff
 */
public class KeyValueFormatException extends IllegalArgumentException {

    public KeyValueFormatException() {
        super();
    }

    public KeyValueFormatException(String message) {
        super(message);
    }

    public KeyValueFormatException(Throwable cause) {
        super(cause);
    }

    public KeyValueFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
