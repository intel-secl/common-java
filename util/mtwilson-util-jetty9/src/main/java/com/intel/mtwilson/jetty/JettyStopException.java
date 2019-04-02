/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jetty;

/**
 *
 * @author jbuhacoff
 */
public class JettyStopException extends JettyException {
    /**
     * 
     * @param message providing some context
     */
    public JettyStopException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message providing some context
     * @param cause 
     */
    public JettyStopException(String message, Throwable cause) {
        super(message, cause);
    }
}
