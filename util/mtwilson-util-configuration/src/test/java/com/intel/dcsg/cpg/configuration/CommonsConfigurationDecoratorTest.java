/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CommonsConfigurationDecoratorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommonsConfigurationDecoratorTest.class);

    @Test
    public void testCsv() {
        org.apache.commons.configuration.PropertiesConfiguration ccPropertiesConfiguration = new org.apache.commons.configuration.PropertiesConfiguration();
        ccPropertiesConfiguration.addProperty("color", "blue");
        ccPropertiesConfiguration.addProperty("color", "red");
        log.debug("color: {}", ccPropertiesConfiguration.getList("color"));
    }
}
