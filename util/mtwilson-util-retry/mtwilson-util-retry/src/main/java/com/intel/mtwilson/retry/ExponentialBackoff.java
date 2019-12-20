/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.retry;

import org.apache.commons.lang3.RandomUtils;

/**
 * Example:
 * Backoff backoff = new ExponentialBackoff(1024); // up to 1024
 * backoff.getMilliseconds();  // 0 .. 1
 * backoff.getMilliseconds();  // 0 .. 2
 * backoff.getMilliseconds();  // 0 .. 4
 * backoff.getMilliseconds();  // 0 .. 8
 * backoff.getMilliseconds();  // 0 .. 16
 * backoff.getMilliseconds();  // 0 .. 32
 * backoff.getMilliseconds();  // 0 .. 64
 * backoff.getMilliseconds();  // 0 .. 128
 * backoff.getMilliseconds();  // 0 .. 256
 * backoff.getMilliseconds();  // 0 .. 512
 * backoff.getMilliseconds();  // 0 .. 1024
 * backoff.getMilliseconds();  // 0 .. 1024
 * backoff.getMilliseconds();  // 0 .. 1024
 * and so on, after reaching the limit the range will not grow any more
 * 
 * The range is [0,max)  inclusive of zero and exclusive of the max, so
 * with a limit of 1024 the biggest range would be [0,1024) which is 
 * 0 &lt;= milliseconds &lt; 1024
 * 
 * @author jbuhacoff
 */
public class ExponentialBackoff implements Backoff {
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExponentialBackoff.class);
    private final long base;
    private long exponent=0, max=Long.MAX_VALUE;

    public ExponentialBackoff() {
        this.base = 2;
    }
    public ExponentialBackoff(long max) {
        this.base = 2;
        this.max = max;
    }
    
    @Override
    public long getMilliseconds() {
        double range = Math.pow(base, exponent);
//        log.debug("base {} exp {} =>  range 0 .. {} up to limit {}", base, exponent, range, limit);
        if( range < max) {
            exponent++;
        }
        return RandomUtils.nextLong(0, (long)range);
    }

}
