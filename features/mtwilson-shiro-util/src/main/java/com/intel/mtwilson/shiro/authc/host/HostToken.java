/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.host;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;

/**
 *
 * @author jbuhacoff
 */
public class HostToken implements HostAuthenticationToken {
    private String host;
    private String credentials;
    
    public HostToken(String remoteAddress) {
        this.host = remoteAddress;
        this.credentials = remoteAddress;
    }
    
    @Override
    public Object getPrincipal() {
        return host;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public String getHost() {
        return host;
    }
    
}
