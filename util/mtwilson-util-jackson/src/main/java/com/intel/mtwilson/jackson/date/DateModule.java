/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.jackson.date;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Date;

/**
 *
 * @author rksavino
 */
public class DateModule extends SimpleModule {
    
    public DateModule() {
        addSerializer(Date.class, new DateSerializer());
        addDeserializer(Date.class, new DateDeserializer());
    }
    
    @Override
    public String getModuleName() {
        return "DateModule";
    }
    
    @Override
    public Version version() {
        return new Version(1, 0, 0, "com.intel.mtwilson.util", "mtwilson-util-jackson-date", null);
    }
}
