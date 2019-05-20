/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.launcher;

import com.intel.mtwilson.Folders;
import java.io.File;

/**
 * The purpose of the launcher is to provide a deployment-specific environment for the
 * Mt Wilson container. This launcher assumes all Mt Wilson jar files are in a single
 * directory to be loaded by a single classloader. When the container is able to handle
 * multiple classloaders better this would be changed here. 
 * 
 * The launcher requires the following environment variables to be set:
 * MTWILSON_HOME     default /opt/mtwilson on Linux, or user.home\mtwilson on Windows
 * MTWILSON_PASSWORD (if mtwilson.properties is encrypted)
 * MTWILSON_CONF   (probably /etc/intel/cloudsecurity , or /etc/mtwilson)
 * 
 * @author jbuhacoff
 */
public class Main {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Main.class);

    private static final ModuleDirectoryLauncher launcher = new ModuleDirectoryLauncher();
    
    public static void main(String[] args) {
        try {
        String mtwilsonHomePath = Folders.application();
        
        // make sure MTWILSON_HOME exists
        File mtwilsonHome = new File(mtwilsonHomePath);
        if( !mtwilsonHome.exists() ) {
            if(!mtwilsonHome.mkdirs()) {
                throw new IllegalStateException("Cannot create directory: "+mtwilsonHomePath);
            }
        }
        
        // create directory jar file resolver
        launcher.setDirectory(new File(mtwilsonHomePath + File.separator + "java"));
        // create container and load modules
           launcher.launch();
            // start event loop (block in foreground so http module etc can listen for connections)
            launcher.startEventLoop();
        }
        catch(Exception e) {
            log.error("Cannot launch container", e);
        }
    }
}
