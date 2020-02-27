/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.retry;

import org.apache.commons.lang3.RandomUtils;

/**
 *
 * @author jbuhacoff
 */
public class RandomBackoff implements Backoff {
    private final long min, max;

    /**
     * 
     * @param min inclusive
     * @param max exclusive
     */
    public RandomBackoff(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public long getMilliseconds() {
        return RandomUtils.nextLong(min, max);
    }
    
}
