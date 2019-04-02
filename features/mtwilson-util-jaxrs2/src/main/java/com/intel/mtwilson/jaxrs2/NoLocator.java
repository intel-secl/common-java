/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.mtwilson.repository.Locator;

/**
 *
 * @author jbuhacoff
 */
public class NoLocator<T> implements Locator<T> {

    @Override
    public void copyTo(T item) {
       
    }
    
}
