/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson;

import java.io.File;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class FilesystemTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    @Test
    public void testFilesystem() {
        log.debug("Configuration folder: {}", Folders.configuration());
        log.debug("Configuration file: {}", (new MyConfiguration()).getConfigurationFile().getAbsolutePath());
        log.debug("Repository folder: {}", Folders.application()+File.separator+"repository");
        
    }
}
