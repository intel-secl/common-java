/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.util.Date;
import javax.ws.rs.ext.ParamConverter;

/**
 *
 * @author jbuhacoff
 */
public class DateParamConverter implements ParamConverter<Date> {

    @Override
    public Date fromString(String string) {
        return Iso8601Date.valueOf(string);
    }

    @Override
    public String toString(Date t) {
        return Iso8601Date.format(t);
    }
    
}
