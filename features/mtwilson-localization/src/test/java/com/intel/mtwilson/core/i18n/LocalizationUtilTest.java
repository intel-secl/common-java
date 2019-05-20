/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.i18n;

import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LocalizationUtilTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalizationUtilTest.class);

    @Test
    public void testRetrieveLocale() throws IOException {
        String[] availableLocales = LocalizationUtil.getAvailableLocales();
        log.debug("Locales: {}", (String[]) availableLocales);
    }
}
