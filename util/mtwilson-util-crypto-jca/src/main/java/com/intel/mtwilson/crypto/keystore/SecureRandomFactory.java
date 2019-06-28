/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.keystore;

import com.intel.mtwilson.crypto.jca.MtWilsonProvider;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

/**
 *
 * @author jbuhacoff
 */
public class SecureRandomFactory {
    
//    private final static Logger log = LoggerFactory.getLogger(SecureRandomFactory.class);
//    private static final LogUtil.Logger log = new LogUtil.Logger();

   
    public static SecureRandom getInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider provider = Security.getProvider("MtWilson");
        if( provider == null ) {
            Security.addProvider(new MtWilsonProvider());
        }
        return SecureRandom.getInstance("SHA256PRNG", "MtWilson");
    }
    
}
