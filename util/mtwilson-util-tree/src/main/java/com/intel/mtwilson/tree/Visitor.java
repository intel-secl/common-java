/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tree;

/**
 *
 * @author jbuhacoff
 */
public interface Visitor<T> {
    void visit(T item);
}
