/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import com.intel.mtwilson.MyJdbc;
import com.intel.mtwilson.jdbi.util.ExistingConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class MyJdbi {
    private static Logger log = LoggerFactory.getLogger(MyJdbi.class);
  
 public static RpcDAO rpc() throws SQLException {
         try {
             Connection connection = MyJdbc.openConnection();
             log.trace("MyJdbi (mtwilson-core-rpc-jdbi) connection: {}", connection);
        DBI dbi = new DBI(new ExistingConnectionFactory(connection));
        log.trace("MyJdbi (mtwilson-core-rpc-jdbi) created DBI instance: {}", dbi);
    return dbi.open(RpcDAO.class);
         }
         catch(Exception e) {
             throw new RuntimeException(e);
         }
    
  }
}
