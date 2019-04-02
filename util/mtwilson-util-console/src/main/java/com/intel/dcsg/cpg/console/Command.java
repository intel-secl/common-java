/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console;

import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public interface Command {
    void setOptions(Configuration options);
    void execute(String[] args) throws Exception;
}
