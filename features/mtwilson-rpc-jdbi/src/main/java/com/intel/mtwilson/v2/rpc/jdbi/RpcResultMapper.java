/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RpcResultMapper implements ResultSetMapper<Rpc> {

    @Override
    public Rpc map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        UUID uuid = UUID.valueOf(rs.getString("ID")); // use this when uuid is a char type in database
        Rpc rpc = new Rpc();
        rpc.setId(uuid);
        rpc.setName(rs.getString("Name"));
        rpc.setStatus(Rpc.Status.valueOf(rs.getString("Status")));
        rpc.setCurrent(rs.getLong("ProgressCurrent"));
        rpc.setMax(rs.getLong("ProgressMax"));
        return rpc;
    }
    
}
