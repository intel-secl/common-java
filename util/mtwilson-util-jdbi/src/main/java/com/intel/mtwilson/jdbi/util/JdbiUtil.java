/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jdbi.util;

import org.skife.jdbi.v2.DBI;
import java.sql.Connection;

/**
 *
 * @author jbuhacoff
 */
public class JdbiUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbiUtil.class);

    public static DBI getDBI(Connection connection) {
        log.debug("JdbiUtil (mtwilson-util-jdbi) using connection: {}", connection);
      // issue #4978: creating new DBI instance for each request
     log.debug("JdbiUtil (mtwilson-util-jdbi) created new DBI instance");
     return new DBI(new ExistingConnectionFactory(connection));
        
    }
}
