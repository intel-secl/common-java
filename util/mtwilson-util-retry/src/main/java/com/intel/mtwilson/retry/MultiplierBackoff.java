/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.retry;

/**
 * 
 * @author jbuhacoff
 */
public class MultiplierBackoff implements Backoff {
    private final long multiplier;
    private final Backoff backoff;

    public MultiplierBackoff(long multiplier, Backoff backoff) {
        this.multiplier = multiplier;
        this.backoff = backoff;
    }

    @Override
    public long getMilliseconds() {
        return multiplier * backoff.getMilliseconds();
    }
    
}
