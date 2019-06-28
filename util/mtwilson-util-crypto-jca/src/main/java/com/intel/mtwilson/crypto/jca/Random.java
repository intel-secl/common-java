/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.security.SecureRandom;

/**
 *
 * @author jbuhacoff
 */
public class Random {
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Random.class);
    private static final LogUtil.Logger log = new LogUtil.Logger();
    private final SecureRandom generator;

    public Random(SecureRandom generator) {
        this.generator = generator;
        log.debug("Initialized random number generator with provider: {}", generator.getProvider().getName());
    }

    public byte[] randomByteArray(int size) {
        byte[] buffer = new byte[size];
        generator.nextBytes(buffer);
        return buffer;
    }
    
}
