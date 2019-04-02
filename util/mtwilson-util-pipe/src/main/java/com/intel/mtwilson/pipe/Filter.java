/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.pipe;

/**
 *
 * @author jbuhacoff
 */
public interface Filter<T> {
    boolean accept(T item);
}
