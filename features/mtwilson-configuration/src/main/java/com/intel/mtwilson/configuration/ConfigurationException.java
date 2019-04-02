/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.configuration;

import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationException extends IOException {
    /**
     * 
     * @param message about storing or loading the configuration
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message about storing or loading the configuration
     * @param cause 
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
