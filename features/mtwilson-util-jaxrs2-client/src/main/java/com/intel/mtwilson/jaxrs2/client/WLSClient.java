/*
 * Copyright (C) 2020 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;

import java.util.Properties;

public class WLSClient extends MtWilsonClient{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AASClient.class);

    public WLSClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }

    public String getVmReportByVmID(String instanceID) {
        log.debug("target: {}", getTarget().getUri().toString());
        String report = getTarget()
                .path("/reports")
                .queryParam("instance_id", instanceID)
                .queryParam("latest_per_vm", "true")
                .request()
                .get(String.class);
        return report;
    }
}
