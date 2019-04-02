/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Properties;
import java.util.Set;

/**
 * If the property is null (missing) or has an empty string value, the
 *  getXYZ methods will return null with the exception of getString which
 * will return either null or empty string.
 * 
 * @author jbuhacoff
 */
public class PropertiesConfiguration extends AbstractConfiguration {
    private Properties properties;
    
    public PropertiesConfiguration() {
        this(new Properties());
    }
    public PropertiesConfiguration(Properties properties) {
        super();
        this.properties = properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
        
    public Properties getProperties() { return properties; }


    @Override
    public Set<String> keys() {
        return properties.stringPropertyNames();
    }

    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }    
    @Override
    public void set(String key, String value) {
        if( value == null ) {
            properties.remove(key);
        }
        else {
            properties.setProperty(key, value);
        }
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    /**
     * Creates a new Properties instance from any Configuration instance
     * and copies all the settings to it
     * 
     * @param configuration
     * @return 
     */
    public static Properties toProperties(Configuration configuration) {
        if (configuration == null || configuration.keys() == null || configuration.keys().isEmpty()) {
            return null;
        }
        
        Properties properties = new Properties();
        for(String key : configuration.keys()) {
            properties.setProperty(key, configuration.get(key));
        }
        return properties;
    }
}
