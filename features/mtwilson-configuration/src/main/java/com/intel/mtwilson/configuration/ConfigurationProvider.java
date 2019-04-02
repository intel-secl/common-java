/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface ConfigurationProvider {
    Configuration load() throws IOException;
    void save(Configuration configuration) throws IOException;
}
