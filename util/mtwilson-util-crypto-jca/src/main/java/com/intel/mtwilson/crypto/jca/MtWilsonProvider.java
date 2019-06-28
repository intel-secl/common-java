/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.security.Provider;

/**
 * Defines "SHA256PRNG" implemented by BouncyCastleSecureRandomSpi
 * Defines "MTWKS" keystore format with modern algorithms
 *
 * @author jbuhacoff
 */
public final class MtWilsonProvider extends Provider {

    public MtWilsonProvider() {
        super("MtWilson", 1.0, "MtWilson provider v1.0");
        put("SecureRandom.SHA256PRNG", "com.intel.mtwilson.crypto.jca.BouncyCastleSecureRandomGeneratorSHA256DigestSpi");
        put("KeyStore.MTWKS", "com.intel.mtwilson.crypto.jca.MtWilsonKeyStorePBKDF2HMACSHA256AESCBCSpi");
    }
}
