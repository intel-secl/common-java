/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.configuration.jaxrs.server;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocument;
import com.intel.mtwilson.configuration.jaxrs.Setting;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/configuration-settings") 
public class ConfigurationSettingsResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationSettingsResource.class);
    CacheControl cc=new CacheControl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("configuration_settings:retrieve")        
    public Response retrieveConfigurationSettings() {
        try {
            ConfigurationDocument document = new ConfigurationDocument();
            Configuration configuration = ConfigurationFactory.getConfiguration();
            document.copyFrom(configuration);

            /*Set Cache Control and Pragma in response.
            Set NoTransform as false as it is set true by default*/
            cc.setNoCache(true);
            cc.setNoStore(true);
            cc.setNoTransform(false);
            return Response.status(Response.Status.OK).entity(document).cacheControl(cc).header("Pragma", "no-cache").build();
        }
        catch(IOException e) {
            log.error("Cannot retrieve configuration settings", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions("configuration_settings:store")        
    public Response storeConfigurationSettings(ConfigurationDocument update) {
        try {
            ConfigurationProvider provider = ConfigurationFactory.getConfigurationProvider();
            Configuration configuration = provider.load();
            if( !configuration.isEditable() ) {
                throw new IllegalStateException("Configuration not editable");
            }
            for(Setting setting : update.getSettings()) {
                configuration.set(setting.getName(), setting.getValue());
            }
            provider.save(configuration);
            // now return an updated document
            ConfigurationDocument document = new ConfigurationDocument();
            document.copyFrom(configuration);

            /*Set Cache Control  and Pragma in response.
            Set NoTransform as false as it is set true by default */
            cc.setNoCache(true);
            cc.setNoStore(true);
            cc.setNoTransform(false);
            return Response.status(Response.Status.OK).entity(document).cacheControl(cc).header("Pragma","no-cache").build();
        }
        catch(IOException | IllegalStateException e) {
            log.error("Cannot store configuration settings", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
