/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.env;

import com.intel.mtwilson.Folders;
import java.io.File;
import org.apache.shiro.config.IniFactorySupport;
import org.apache.shiro.web.env.IniWebEnvironment;

/**
 * Reference:
 * https://shiro.apache.org/static/1.2.2/apidocs/org/apache/shiro/web/env/EnvironmentLoader.html
 * https://shiro.apache.org/web.html (look for shiroEnvironmentClass)
 * https://shiro.apache.org/configuration.html
 * 
 * The EnvironmentLoader publishes the loaded environment as a ServletContext
 * attribute with key org.apache.shiro.web.env.EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY
 * (it looks like a variable name and it is -- and its value is the fully qualified
 * package, class, and variable name as shown)
 * 
 * @author jbuhacoff
 */
public class LocalIniWebEnvironment extends IniWebEnvironment {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalIniWebEnvironment.class);

    @Override
    protected String[] getDefaultConfigLocations() {
        log.debug("getDefaultConfigLocations {}", Folders.configuration()+File.separator+"shiro.ini");
        return new String[] {
            "file:///"+(Folders.configuration()+File.separator+"shiro.ini").replace(File.separator,"/"), // without file prefix its interpreted as relative to webapp
            IniWebEnvironment.DEFAULT_WEB_INI_RESOURCE_PATH,
            IniFactorySupport.DEFAULT_INI_RESOURCE_PATH
        };
    }

}
