/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.feature;

import java.io.File;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JavaClasspathFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaClasspathFactoryTest.class);

    @Test
    public void testFeatureClasspath() {
        Set<File> jars = JavaClasspathFactory.findAllFeatureJars();
        log.debug("Found {} jars", jars.size());
    }
}
