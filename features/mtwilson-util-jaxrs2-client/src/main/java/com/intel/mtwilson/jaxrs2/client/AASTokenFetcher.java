/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.UserCredential;

import java.util.Properties;

/**
 * @author rawatar
 */
public class AASTokenFetcher {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AASTokenFetcher.class);

    public String getAASToken(String username, String password, TlsConnection tlsConnection) throws Exception {
        AASClient aasClient = new AASClient(new Properties(), tlsConnection);
        UserCredential credential = new UserCredential();
        credential.setUsername(username);
        credential.setPassword(password);
        return aasClient.getToken(credential);
    }
}
