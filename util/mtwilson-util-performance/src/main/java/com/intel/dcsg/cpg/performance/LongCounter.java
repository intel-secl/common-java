/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.performance;

/**
 * Implements a Counter interface with Long values. Adds a longValue() method
 * for obtaining the current count.
 * @author jbuhacoff
 */
public class LongCounter implements Counter,Value<Long> {
    private long count = 0;
    
    @Override
    public void increment() {
        count++;
    }
    public long longValue() { return count; }
    
    @Override
    public Long getValue() { return count; }
}
