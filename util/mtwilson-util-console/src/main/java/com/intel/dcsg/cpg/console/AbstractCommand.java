/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclasses must implement execute() from Command.
 * Any errors encountered inside execute() should be thrown as exceptions.
 * @author jbuhacoff
 */
public abstract class AbstractCommand implements Command {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected Configuration options = null;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    public Configuration getOptions() {
        return options;
    }
}
