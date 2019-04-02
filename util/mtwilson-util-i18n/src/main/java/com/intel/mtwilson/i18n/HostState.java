/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.i18n;

/**
 * @author rksavino
 */
public enum HostState {
    CONNECTED("Connected"),
    QUEUE("In queue"),
    CONNECTION_FAILURE("Connection failure"),
    CONNECTION_TIMEOUT("Connection timeout"),
    UNAUTHORIZED("Unauthorized"),
    AIK_NOT_PROVISIONED("AIK certificate is not provisioned"),
    EC_NOT_PRESENT("Endorsement certificate is not present"),
    MEASURED_LAUNCH_FAILURE("TXT measured launch failure"),
    TPM_OWNERSHIP_FAILURE("TPM ownership failure"),
    TPM_NOT_PRESENT("TPM is not present"),
    UNSUPPORTED_TPM("Unsupported TPM version"),
    UNKNOWN("Unknown host state");
    
    String hostStateText;
    
    public String getHostStateText() {
        return hostStateText;
    }
    
    public void setHostStateText(String hostStateText) {
        this.hostStateText = hostStateText;
    }
    
    public static HostState getHostState(String hostStateText) {
        for (HostState hostState : HostState.values()) {
            if (hostState.getHostStateText().equals(hostStateText))
                return hostState;
        }
        return UNKNOWN;
    }
    
    private HostState(String hostStateText) {
        this.hostStateText = hostStateText;
    }
}
