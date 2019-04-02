/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util;

//import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.i18n.LocalizableResponseFilter;
import com.intel.mtwilson.core.i18n.LocalizationUtil;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;

/**
 *
 * @author jbuhacoff
 */
@Priority(Priorities.ENTITY_CODER)
public class ASLocalizationFilter extends LocalizableResponseFilter {
    public ASLocalizationFilter() throws IOException {
        setAvailableLocales(LocalizationUtil.getAvailableLocales());
//        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
