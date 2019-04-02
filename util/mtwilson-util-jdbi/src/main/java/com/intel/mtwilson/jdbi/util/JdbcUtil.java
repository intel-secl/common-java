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

    /*  Commented out getDataSource and getConnection because it appears they
     *  are not being used at all, and cannot use "My" from here
    public static DataSource getDataSource() {        
        try {
            if (ds == null) {
                String driver = My.jdbc().driver();
                String dbUrl = My.jdbc().url();
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
                dataSource.setUrl(dbUrl);
                Configuration configuration = ConfigurationFactory.getConfiguration();
                dataSource.setUsername(My.configuration().getDatabaseUsername());
                dataSource.setPassword(My.configuration().getDatabasePassword());
                ds = dataSource;
            }
        } catch (Exception ex) {
            log.error("Error connecting to the database. {}", ex.getMessage());
        }
        return ds;
    }

     public static Connection getConnection() {
        try {
            if (conn == null) {
                conn = getDataSource().getConnection();
            }
            return conn;
        } catch (Exception ex) {
            log.error("Error connection to the database. {}", ex.getMessage());
        }
        return null;
    }    
    */
    
    
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
