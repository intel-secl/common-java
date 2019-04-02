/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LogObserver<T> implements Observer<T> {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void observe(T object) {
        log.debug("Observed: {}", object.toString());
    }
    
}
