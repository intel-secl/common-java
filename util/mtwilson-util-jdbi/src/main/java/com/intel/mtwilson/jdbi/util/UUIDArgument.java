/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jdbi.util;

import com.intel.dcsg.cpg.io.UUID;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows UUID instances to be passed directly to DAO queries as
 * parameters. It automatically converts the UUID to either a 16-byte binary
 * field or a 36 byte text field (32 bytes hex and 4 hyphens)
 *
 * References: https://groups.google.com/forum/#!topic/jdbi/VxEmvWwshso
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html
 * http://stackoverflow.com/questions/12022452/does-jdbi-accept-uuid-parameters
 * https://groups.google.com/forum/?fromgroups=#!searchin/jdbi/argument$20factory/jdbi/ooFw_s183jM/WLwNBJuemYEJ
 * https://groups.google.com/forum/#!topic/jdbi/YvVP1bwqYcg
 *
 * @author jbuhacoff
 */
public class UUIDArgument implements ArgumentFactory<UUID> {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean accepts(Class<?> type, Object value, StatementContext ctx) {
        return value != null && UUID.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Argument build(Class<?> type, final UUID value, StatementContext ctx) {
        return new Argument() {
            @Override
            public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
                statement.setString(position, value.toString());
            }

            /**
             * Useful for seeing the actual uuid in error messages, for example
             * in SQL exceptions, even if the exception is not related to the
             * UUID , like if some other parameter had an error, but UUID was in
             * the statement so it's value is shown.
             */
            @Override
            public String toString() {
                return value.toString();
            }
        };
    }
}
