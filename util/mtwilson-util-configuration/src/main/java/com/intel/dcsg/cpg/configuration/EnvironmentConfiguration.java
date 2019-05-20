/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 * The keys are case-sensitive.
 * 
 * @author jbuhacoff
 */
public class EnvironmentConfiguration extends AbstractConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnvironmentConfiguration.class);
    
    public EnvironmentConfiguration() {
        super();
    }

    @Override
    public Set<String> keys() {
        return System.getenv().keySet();
    }

    @Override
    public String get(String key) {
        String value = System.getenv(key);
        return value;
    }

    @Override
    public void set(String key, String value) {
        throw new UnsupportedOperationException("Cannot write to environment");
    }

    @Override
    public boolean isEditable() {
        return false;
    }
    
    

}
