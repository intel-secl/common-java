/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mountwilson.as.common;

import com.intel.dcsg.cpg.configuration.CommonsConfigurationAdapter;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to use commons-configuration to load the Attestation Service
 * settings.
 *
 * The configuration is loaded in the following priority order: System
 * properties Properties in the file attestation-service.properties (create this
 * file in your classpath or home directory to customize local settings)
 * Properties in the file attestation-service-defaults.properties (included with
 * ASCommon) Hard-coded defaults (defined in this class)
 *
 * The attestation-service.properties file can be placed in your home directory
 * in order to customize the application settings for your machine.
 *
 * @author jabuhacx
 */
public class ASConfig {

    private static final ASConfig global = new ASConfig();

    public static Configuration getConfiguration() {
        try {
            return new CommonsConfigurationAdapter(ConfigurationFactory.getConfiguration());
        } catch (IOException e) {
            log.error("Cannot load configuration", e);
            return new CommonsConfigurationAdapter(new PropertiesConfiguration());
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ASConfig.class);

    public static Properties getDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty("com.intel.mountwilson.as.home", "C:/work/aikverifyhome"); // used by TAHelper
        defaults.setProperty("com.intel.mountwilson.as.openssl.cmd", "openssl.bat"); // used by TAHelper
        defaults.setProperty("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe"); // used by TAHelper
        defaults.setProperty("daa.enabled", "false");
        defaults.setProperty("com.intel.mountwilson.as.trustagent.timeout", "3"); // seconds
        defaults.setProperty("com.intel.mountwilson.as.attestation.hostTimeout", "30");  // seconds

        // Setting to control the # of parallel threads & associated time out for supporting multithreading during CRUD operations on hosts
        defaults.setProperty("mtwilson.bulkmgmt.threads.max", "32");
        defaults.setProperty("com.intel.mountwilson.as.hostmgmt.hostTimeout", "600");
        return defaults;
    }

    public static int getTrustAgentTimeOutinMilliSecs() {
        // Return timeout in milliseconds
        return getConfiguration().getInt("com.intel.mountwilson.as.trustagent.timeout", 3) * 1000;
    }
}
