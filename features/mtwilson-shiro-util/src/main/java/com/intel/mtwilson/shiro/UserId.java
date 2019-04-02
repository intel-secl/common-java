/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;

/**
 *
 * @author jbuhacoff
 */
public class UserId implements Serializable {
    private static final long serialVersionUID = 25247225622776147L;
    private UUID userId;

    protected UserId() { }
    
    

    public UserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
    
}
