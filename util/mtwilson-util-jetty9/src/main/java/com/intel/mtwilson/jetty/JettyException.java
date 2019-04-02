/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jetty;

/**
 *
 * @author jbuhacoff
 */
public class JettyException extends Exception {
    /**
     * 
     * @param message providing some context
     */
    public JettyException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message providing some context
     * @param cause 
     */
    public JettyException(String message, Throwable cause) {
        super(message, cause);
    }
}
