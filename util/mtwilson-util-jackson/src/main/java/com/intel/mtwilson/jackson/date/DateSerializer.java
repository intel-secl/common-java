/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.jackson.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author rksavino
 */
public class DateSerializer extends JsonSerializer<Date> {
    
    @Override
    public void serialize(Date t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        jg.writeString(Iso8601Date.format(t));
    }
}
