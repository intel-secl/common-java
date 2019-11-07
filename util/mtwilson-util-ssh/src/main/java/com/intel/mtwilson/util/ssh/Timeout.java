/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.util.concurrent.TimeUnit;

/**
 * Can be replaced with Duration class when upgrading to Java 8
 * @author jbuhacof
 */
public class Timeout {
    private final long millis;

    public Timeout(long millis) {
        this.millis = millis;
    }
    
    public Timeout(long duration, TimeUnit units) {
        this.millis = units.toMillis(duration);
    }
    
    public long getMillis() {
        return millis;
    }
}
