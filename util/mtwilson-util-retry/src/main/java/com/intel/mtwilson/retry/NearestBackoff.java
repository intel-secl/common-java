/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.retry;

/**
 * Rounding to "nearest 5ms" for example
 * 
 * @author jbuhacoff
 */
public class NearestBackoff implements Backoff {
    private final long nearest;
    private final Backoff backoff;

    public NearestBackoff(long nearest, Backoff backoff) {
        this.nearest = nearest;
        this.backoff = backoff;
    }

    @Override
    public long getMilliseconds() {
        return ((backoff.getMilliseconds() + nearest-1)/nearest) * nearest;
    }
    
}
