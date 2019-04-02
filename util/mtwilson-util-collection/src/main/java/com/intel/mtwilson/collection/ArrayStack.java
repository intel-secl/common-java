/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.collection;

import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class ArrayStack<E> extends ArrayList<E> implements Stack<E> {

    @Override
    public void push(E item) {
        add(item);
    }

    @Override
    public E pop() {
        int size = size();
        E item = get(size-1); // last element, throws IndexOutOfBoundsException if stack is empty
        remove(size-1);
        return item;
    }
    
}
