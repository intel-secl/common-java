/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.jca;

import java.security.Provider;

/**
 * Defines "SHA384PRNG" implemented by BouncYCastleSecureRandomSpi
 * @author jbuhacoff
 */
public final class MtWilsonProvider extends Provider {
    public MtWilsonProvider() {
        super("MtWilson", 1.0, "MtWilson provider v1.0");
        put("SecureRandom.SHA384PRNG", "com.intel.dcsg.cpg.crypto.jca.BouncyCastleSecureRandomGeneratorSHA384DigestSpi");
    }
}
