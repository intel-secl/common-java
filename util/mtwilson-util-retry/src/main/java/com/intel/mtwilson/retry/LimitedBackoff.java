/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.retry;


/**
 * Example:
 * Using ExponentialBackoff(1024) gives a range 0..1, 0..2, etc. up to 0..1024
 * but when using that to delay ssh requests, we don't really want to wait just 
 * 0ms or 1ms, it's too short.  So use MultiplierBackoff to multiply any 
 * value returned from another Backoff implementation ,and use MinimumBackoff
 * to ensure that we wait at least 50ms, for example when the random backoff is 0ms.
 * 
 * Backoff backoff = new MinimumBackoff(50, new MultiplierBackoff(100, new ExponentialBackoff(1024))); // up to 102,400 ~ 102sec ~ 1min42sec
 * backoff.getMilliseconds();  // 50, 100
 * backoff.getMilliseconds();  // 50, 100, 200
 * backoff.getMilliseconds();  // 50, 100, 200, 300, 400
 * backoff.getMilliseconds();  // 50 .. 800 etc. 
 * and so on
 * 
 * 
 * @author jbuhacoff
 */
public class LimitedBackoff implements Backoff {
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExponentialBackoff.class);
    private long min = 0, max = Long.MAX_VALUE;
    private final Backoff backoff;

    public LimitedBackoff(long min, long max, Backoff backoff) {
        this.min = min;
        this.max = max;
        this.backoff = backoff;
    }

    @Override
    public long getMilliseconds() {
        long value = backoff.getMilliseconds();
        if( value < min ) { return min; }
        if( value > max ) { return max; }
        return value;
    }
    
}
