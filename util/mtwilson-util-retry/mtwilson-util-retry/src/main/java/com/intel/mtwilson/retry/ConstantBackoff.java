/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.retry;

/**
 *
 * @author jbuhacoff
 */
public class ConstantBackoff implements Backoff {
    private final long milliseconds;

    public ConstantBackoff(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Override
    public long getMilliseconds() {
        return milliseconds;
    }
    
}
