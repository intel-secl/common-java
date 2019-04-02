/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jca.setup;

import com.intel.mtwilson.crypto.jca.ProviderUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.List;

/**
 * @author jbuhacoff
 */
public class JcaSecurityProviders extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JcaSecurityProviders.class);

    private final String[] providers = new String[] { "org.bouncycastle.jce.provider.BouncyCastleProvider", "com.intel.mtwilson.crypto.jca.MtWilsonProvider" };
    
    public JcaSecurityProviders() {
        super();
    }
    
    @Override
    protected void configure() throws Exception {
        File javaSecurityFile = ProviderUtil.getJavaSecurityFile();
        log.debug("java.security path: {}", javaSecurityFile.getAbsolutePath());
    }

    @Override
    protected void validate() throws Exception {
        List<String> installedProviders = ProviderUtil.readSecurityProviderList();
        for(String provider : providers) {
            if( !installedProviders.contains(provider) ) {
                validation(String.format("Provider not yet installed in java.security file: %s", provider));
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        List<String> installedProviders = ProviderUtil.readSecurityProviderList();
        // add missing providers to the java.security file
        for(String provider : providers) {
            if( !installedProviders.contains(provider) ) {
                log.debug("Appending security provider: {}", provider);
                ProviderUtil.appendSecurityProvider(provider);
            }
        }
        // load missing providers in memory in case any other setup tasks
        // in the same VM require them, so app doesn't have to reload
        for(String provider : providers) {
            if( !installedProviders.contains(provider) ) {
                log.debug("Loading security provider: {}", provider);
                Class providerClass = Class.forName(provider);
                Provider providerInstance = (Provider)providerClass.newInstance();
                Security.addProvider(providerInstance);
            }
        }
    }
    
}
