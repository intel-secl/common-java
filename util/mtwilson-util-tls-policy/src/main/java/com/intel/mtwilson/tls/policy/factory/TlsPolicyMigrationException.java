/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.factory;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyMigrationException extends RuntimeException {
    private String hostId;
    
    public TlsPolicyMigrationException() {
        super();
    }

    public TlsPolicyMigrationException(String message) {
        super(message);
    }

    public TlsPolicyMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TlsPolicyMigrationException(String message, Throwable cause, String hostId) {
        super(message, cause);
        this.hostId = hostId;
    }

    public String getHostId() {
        return hostId;
    }
    
    
}
