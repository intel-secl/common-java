/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionNotFoundException extends UnsupportedOperationException {
    /**
     * 
     * @param message typically the name of the extension, such as a fully qualified java class name
     */
    public ExtensionNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message typically the name of the extension, such as a fully qualified java class name
     * @param cause 
     */
    public ExtensionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
