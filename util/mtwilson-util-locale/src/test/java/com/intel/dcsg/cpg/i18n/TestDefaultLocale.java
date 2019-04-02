/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.i18n;

import java.util.Locale;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestDefaultLocale {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestDefaultLocale.class);
    
    @Test
    public void testGetDefaultLocale() {
        log.debug("default locale: {}", LocaleUtil.toLanguageTag(Locale.getDefault()));
        log.debug("user.language: {}", System.getProperty("user.language"));
        log.debug("user.country: {}", System.getProperty("user.country"));
    }
}
