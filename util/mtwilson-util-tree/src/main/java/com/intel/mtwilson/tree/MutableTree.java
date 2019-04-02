/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tree;

/**
 *
 * @author jbuhacoff
 */
public interface MutableTree<T> extends Tree<T> {
    void remove(T node);
    // maybe   insert(T parent, T child); to insert a new child node into the parent node ... but what about position?  insertAt(parent, index, child) ?  with insert(parent,child) assuming index 0 so insert at the front? 
}
