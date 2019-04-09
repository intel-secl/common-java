/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.shiro.ShiroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */

@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<ShiroException> {
    private static Logger log = LoggerFactory.getLogger(AuthorizationExceptionMapper.class);

    @Override
    public Response toResponse(ShiroException e) {
        log.debug("Shiro {}: {}", e.getClass().getName(), e.getMessage(), e);
        Response response = Response.status(Status.UNAUTHORIZED).build();
        return response;
    }
    
}
