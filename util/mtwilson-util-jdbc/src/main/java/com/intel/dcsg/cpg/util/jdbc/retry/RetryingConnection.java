/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.util.jdbc.retry;

import com.intel.dcsg.cpg.util.jdbc.DelegatingConnection;
import java.sql.Connection;
import javax.sql.DataSource;

/**
 * A delegating connection that will automatically retry an action if it catches
 * an exception that indicates temporary link failure (idle too long, database
 * server restarted, etc)
 *
 * The retrying connection needs both an existing connection and a datasource to
 * provide replacement connections. If a retryable error is caught the existing
 * connection is closed and a new connection is obtained from the datasource.
 *
 * This functionality is completely separated from the connection pool and any
 * data source can be used to obtain new connections. It is recommended to wrap
 * connections received from the pool with this delegate so that the pool
 * functionality will not be affected.
 *
 * <pre>
 * [poolable] data source -- retrying connection -- application
 * </pre>
 *
 * TODO: use javassist to generate this proxy dynamically; only shortcoming is
 * the wrapped connection must have a no-arg constructor
 *
 * @author jbuhacoff
 */
public class RetryingConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryingConnection.class);

    protected DataSource ds;

    public RetryingConnection(Connection connection, DataSource ds) {
        super(connection);
        this.ds = ds;
    }

}
