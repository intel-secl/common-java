/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.jaxrs2.UserCredential;

import java.net.URL;
import java.util.Properties;

/**
 * @author rawatar
 */
public class AASTokenFetcher {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AASTokenFetcher.class);

    public String getAASToken(String aasApiUrl, String username, String password) throws Exception {
        log.debug("Using AAS API URL to fetch token - {}", aasApiUrl);
        TlsConnection tlsConnection = new TlsConnection(new URL(aasApiUrl), new InsecureTlsPolicy());
        Properties clientConfiguration = new Properties();
        AASClient aasClient = new AASClient(clientConfiguration, tlsConnection);
        UserCredential credential = new UserCredential();
        credential.setUsername(username);
        credential.setPassword(password);
        return aasClient.getToken(credential);
    }
}
