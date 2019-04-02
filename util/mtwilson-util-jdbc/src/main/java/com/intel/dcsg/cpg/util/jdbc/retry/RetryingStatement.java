/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.util.jdbc.retry;

import com.intel.dcsg.cpg.util.jdbc.DelegatingStatement;
import java.sql.Statement;

/**
 *
 * @author jbuhacoff
 */
public class RetryingStatement extends DelegatingStatement {
    protected RetryingConnection connection;
    public RetryingStatement(Statement statement, RetryingConnection connection) {
        super(statement);
        this.connection = connection;
    }
}
