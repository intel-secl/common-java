/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;

/**
 *
 * @author jbuhacoff
 */
public class AnyProtocolSelector implements ProtocolSelector {

    @Override
    public boolean accept(String protocolName) {
        return true;
    }

    @Override
    public String preferred() {
        return "TLSv1.2";
    }
    
}
