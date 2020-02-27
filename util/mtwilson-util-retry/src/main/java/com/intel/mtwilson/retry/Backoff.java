/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.retry;

/**
 *
 * @author jbuhacoff
 */
public interface Backoff {
    long getMilliseconds();
}
