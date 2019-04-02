/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.setup.ws;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="setup_task")
public class SetupTask extends Document {
    
    private String name;
    private HashMap<String,String> properties = new HashMap<String,String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
    
    
    
}
