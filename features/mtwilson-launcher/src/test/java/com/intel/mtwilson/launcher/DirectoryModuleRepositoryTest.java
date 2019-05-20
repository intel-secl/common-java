/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.module.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.commons.lang3.StringUtils;

/**
 * The Maven pom.xm for this module specifies a few modules to copy into the "target" folder during the build;
 * we try to load classes from those modules in order to make the test repeatable on different developer machines.
 * 
 * @author jbuhacoff
 */
public class DirectoryModuleRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectoryModuleRepositoryTest.class);
    
    private void printCollection(String label, Collection<Object> values) {
        log.debug(label+": "+StringUtils.join("|", values));
    }
    public void printModuleManifest(String jarfilePath) throws IOException {
        FileInputStream in = new FileInputStream(new File(jarfilePath));
        JarInputStream jarStream = new JarInputStream(in);
        Manifest mf = jarStream.getManifest();
        if( mf == null ) {
            log.debug("No manifest in {}", jarfilePath);
            return;
        }
        // the main attributes are what manifest.mf files typically have;
        // the named attributes accessible via getEntries and getAttributes(name) are
        // actually SEPARATE SECTIONS  in the manifest.mf file and not the headers/attributes
        // themselves.  so that shows up empty most of the time.
        Attributes main = mf.getMainAttributes();
        String title = main.getValue(Attributes.Name.IMPLEMENTATION_TITLE); // Implementation-Title
        String version = main.getValue(Attributes.Name.IMPLEMENTATION_VERSION); // Implementation-Version
        String vendor = main.getValue(Attributes.Name.IMPLEMENTATION_VENDOR); // Implementation-Vendor
        String classpath = main.getValue(Attributes.Name.CLASS_PATH); // Class-Path
        String moduleComponents = main.getValue(Module.MODULE_COMPONENTS); // Module-Components
        log.debug("title: {}", title);
        log.debug("version: {}", version);
        log.debug("vendor: {}", vendor);
        log.debug("classpath: {}", classpath);
        log.debug("modules: {}", moduleComponents);
        jarStream.close();
    }
}
