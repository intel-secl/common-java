/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClasspathTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathTest.class);

    @Test
    public void printClassPath() {
        String classpath = System.getProperty("java.class.path");
        log.debug("classpath: {}", classpath);
        log.debug("path separator: {}", File.pathSeparator);
        String[] entries = StringUtils.split(classpath, System.getProperty("path.separator")); 
        for(String entry : entries) {
            log.debug("entry: {}", entry);
        }
    }
}
