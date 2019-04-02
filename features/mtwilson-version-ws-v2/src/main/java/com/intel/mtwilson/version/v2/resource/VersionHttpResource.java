/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.version.v2.resource;

import com.intel.mtwilson.version.v2.model.Version;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/version")
public class VersionHttpResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VersionHttpResource.class);
    private static final Version version = new Version();
    
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,DataMediaType.APPLICATION_YAML,DataMediaType.TEXT_YAML})
    public Version queryVersion(@Context final HttpServletResponse response) {
        log.debug("retrieve version");
        return version;
    }
    
}
