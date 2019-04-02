/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;
import java.security.Principal;

/**
 *
 * @author jbuhacoff
 */
public class LoginHostPrincipal implements Serializable, Principal {
    private static final long serialVersionUID = 854727147L;
    private String host;

    protected LoginHostPrincipal() { }
    
    public LoginHostPrincipal(String host) {
        this.host = host;
    }

    @Override
    public String getName() {
        return host;
    }

    public String getHost() {
        return host;
    }
    
}
