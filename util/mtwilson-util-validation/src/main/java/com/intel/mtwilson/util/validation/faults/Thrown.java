/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.validation.faults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class Thrown extends Fault {
    private Throwable cause;
    public Thrown(Throwable cause) {
        super(String.format("[%s: %s]", cause.getClass().getName(), cause.getMessage()));
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }
    public Thrown(Throwable cause, String description) {
        super(description);
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }
    public Thrown(Throwable cause, String format, Object... args) {
        super(format, args);
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }

    @JsonIgnore
    public Throwable getCause() {
        return cause;
    }
    
}
