/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.security.Provider;

/**
 * Defines "SHA384PRNG" implemented by BouncyCastleSecureRandomSpi
 * Defines "MTWKS" keystore format with modern algorithms
 *
 * @author jbuhacoff
 */
public final class MtWilsonProvider extends Provider {

    public MtWilsonProvider() {
        super("MtWilson", 1.0, "MtWilson provider v1.0");
        put("SecureRandom.SHA384PRNG", "com.intel.mtwilson.crypto.jca.BouncyCastleSecureRandomGeneratorSHA384DigestSpi");
        put("KeyStore.MTWKS", "com.intel.mtwilson.crypto.jca.MtWilsonKeyStorePBKDF2HMACSHA256AESCBCSpi");
    }
}
