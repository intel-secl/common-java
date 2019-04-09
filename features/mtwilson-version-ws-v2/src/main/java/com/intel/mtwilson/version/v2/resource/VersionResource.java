/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.version.v2.resource;

import com.intel.mtwilson.version.v2.model.Version;
import com.intel.mtwilson.version.v2.model.VersionFilterCriteria;
import com.intel.mtwilson.version.v2.model.VersionCollection;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/versions")
public class VersionResource extends AbstractResource<Version,VersionCollection,VersionFilterCriteria,NoLinks<Version>> {

    @Override
    protected VersionCollection search(VersionFilterCriteria criteria) {
        VersionCollection versions = new VersionCollection();
        Version version = new Version();
        versions.getVersions().add(version);
        return versions;
    }

    @Override
    protected Version retrieve(String id) {
        if( !UUID.isValid(id) ) { return null; }
        Version version = new Version();
        return version;
    }

    @Override
    protected void create(Version item) {
        // store it...
    }
    @Override
    protected void store(Version item) {
        // store it...
    }

    @Override
    protected void delete(String id) {
    }

    @Override
    protected VersionCollection createEmptyCollection() {
        return new VersionCollection();
    }
    
}
