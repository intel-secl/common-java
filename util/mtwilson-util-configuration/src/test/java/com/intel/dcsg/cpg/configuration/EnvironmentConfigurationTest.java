/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class EnvironmentConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnvironmentConfigurationTest.class);

    @Test
    public void testEnv() {
        EnvironmentConfiguration env = new EnvironmentConfiguration();
        log.debug("JAVA_HOME = {}", env.get("JAVA_HOME"));
    }
}
