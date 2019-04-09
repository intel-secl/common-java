/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jdbi.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.skife.jdbi.v2.tweak.ConnectionFactory;

/**
 *
 * @author jbuhacoff
 */
public class ExistingConnectionFactory implements ConnectionFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExistingConnectionFactory.class);

    private Connection connection;
    public ExistingConnectionFactory(Connection connection) {
        this.connection = connection;
    }

    
    @Override
    public Connection openConnection() throws SQLException {
        log.debug("openConnection returning connection: {}", connection);
        return connection;
    }
}
