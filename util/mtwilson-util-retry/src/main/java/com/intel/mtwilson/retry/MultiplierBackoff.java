/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
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
