/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test.config;

import com.intel.mtwilson.ms.common.MSConfig;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class MSConfigTest {
    @Test
    public void testLoadConfig() {
        Configuration serviceConf = MSConfig.getConfiguration();        
        assertEquals("https://192.168.0.1:8181", serviceConf.getString("mtwilson.api.baseurl"));
    }
}
