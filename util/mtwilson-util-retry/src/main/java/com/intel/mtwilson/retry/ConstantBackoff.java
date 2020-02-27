/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
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
