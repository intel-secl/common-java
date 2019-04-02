/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.faults;

import com.intel.mtwilson.util.validation.faults.Thrown;

/**
 *
 * @author jbuhacoff
 */
public class InvalidRpcInput extends Thrown {
    public InvalidRpcInput(Throwable cause) {
        super(cause);
    }
}
