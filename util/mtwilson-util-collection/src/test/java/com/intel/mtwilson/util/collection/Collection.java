/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.collection;

/**
 * A minimalist, read-only Collection interface 
 * @author jbuhacoff
 */
public interface Collection<T> extends Iterable<T> {
    boolean contains(T object);
    boolean isEmpty();
    int size();
}
