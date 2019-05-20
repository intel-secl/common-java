/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jdbi.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JdbcUtil {

    private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

    public static Connection conn = null;
    public static DataSource ds = null;

    /**
     * Does NOT close the result set.
     *
     * @param rs
     * @throws SQLException
     */
    public static void describeResultSet(ResultSet rs) throws SQLException {
        int columns = rs.getMetaData().getColumnCount();
        log.debug("Result set has {} columns", columns);
        for(int i=1; i<=columns; i++) {
            log.debug(String.format("Column: %s  Data type: %s", rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnTypeName(i)));
        }
    }
}
