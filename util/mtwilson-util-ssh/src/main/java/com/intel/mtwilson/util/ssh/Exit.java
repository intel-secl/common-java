/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.ssh;

import net.schmizz.sshj.connection.channel.direct.Signal;

/**
 *
 * @author jbuhacoff
 */
public class Exit {
    private final Integer code;
    private final String message;
    private final Signal signal;

    public Exit(Integer code, String message, Signal signal) {
        this.code = code;
        this.message = message;
        this.signal = signal;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Signal getSignal() {
        return signal;
    }
    
    
}
