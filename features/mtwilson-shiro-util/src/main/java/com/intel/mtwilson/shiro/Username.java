/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import java.io.Serializable;


/**
 *
 * @author jbuhacoff
 */
public class Username implements Serializable {
    private static final long serialVersionUID = 352226147L;
    private String username;

    protected Username() { }
    
    public Username(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
}
