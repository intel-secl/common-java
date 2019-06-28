/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.util.jdbc;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.objectpool.ObjectPool;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is a decorator for Connection that overrides close() to return the
 * connection to the pool instead of actually closing it. 
 * 
 * The only important methods are the constructor and close. 
 * All other methods are overridden for logging purposes only.
 * 
 * @author jbuhacoff
 */
public class PooledConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PooledConnection.class);
    protected ObjectPool<Connection> pool;
    protected final long id;
    
    public PooledConnection(Connection connection, ObjectPool<Connection> objectPool) {
        super(connection);
        SecureRandom random = RandomUtil.getSecureRandom();
        id = random.nextLong();
        pool = objectPool;
        log.debug("[{}] constructor wrapping {}", id, connection);
    }
    
    @Override
    public void close() throws SQLException {
        log.debug("[{}] close", id);
        pool.returnObject(this);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return super.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return super.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return super.prepareStatement(sql, columnNames);
    }

    @Override
    public String toString() {
        return String.format("PooledConnection[%s] wrapping %s", id, delegate);
    }
   
}
