/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.jackson.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author rksavino
 */
public class DateDeserializer extends JsonDeserializer<Date> {
    
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        return Iso8601Date.valueOf(jp.getValueAsString());
    }
    
}
