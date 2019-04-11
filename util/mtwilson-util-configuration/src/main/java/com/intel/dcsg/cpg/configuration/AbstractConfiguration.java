/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Provides common functionality for Configuration implementations.
 *
 * Subclasses should implement {@code get(String key)} and return null
 * if the key is not found (or it is null). 
 * 
 * Subclasses should implement {@code set(String key, String value)}
 * 
 * Subclasses should implement {@code keys()} and {@code isEditable()}
 * 
 * @author jbuhacoff
 */
public abstract class AbstractConfiguration implements Configuration {

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Searches for the specified key, if found returns its value, otherwise
     * returns the value returned from the defaultValueCallback.
     * 
     * @param key
     * @param defaultValueCallback
     * @return
     * @throws Exception 
     */
    public String get(String key, Callable<String> defaultValueCallback) throws ConfigurationException {
        String value = get(key);
        if (value != null) {
            return value;
        }
        try {
            return defaultValueCallback.call();
        }
        catch(Exception e) {
            throw new ConfigurationException(key, e);
        }
    }
    
    /**
     * Any changes to the returned Map will NOT affect the configuration
     * instance.
     *
     * @return a Map instance with a copy of the configuration settings
     */
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String key : keys()) {
            String value = get(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Any changes to the returned Properties will NOT affect the configuration
     * instance.
     *
     * @return a Properties instance with a copy of the configuration settings
     */
    public Properties toProperties() {
        Properties properties = new Properties();
        for (String key : keys()) {
            String value = get(key);
            if (value != null) {
                properties.setProperty(key, value);
            }
        }
        return properties;
    }
}
