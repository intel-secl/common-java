/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author jbuhacoff
 */
public class KeyPolicy {
    private long keyCounterMax = Integer.MAX_VALUE; // a 32-bit number,  so should be a safe default for AES-128 and stronger algorithms
    private TimeUnit expiresAfter; 

    public TimeUnit getExpiresAfter() {
        return expiresAfter;
    }

    public long getKeyCounterMax() {
        return keyCounterMax;
    }

    
}
