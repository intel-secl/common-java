/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurableProtocolSelector implements ProtocolSelector {
    private final HashSet<String> protocols = new HashSet<String>();
    private String preferred = "TLSv1.2";

    public ConfigurableProtocolSelector() {
    }
    
    public ConfigurableProtocolSelector(String... protocolNames) {
        if( protocolNames.length > 0 ) {
            preferred = protocolNames[0];
        }
        protocols.addAll(Arrays.asList(protocolNames));
    }
    
    public Set<String> getProtocols() { return protocols; } 
    public void setPreferred(String protocolName) { preferred = protocolName; }
    
    @Override
    public boolean accept(String protocolName) {
        return protocols.contains(protocolName);
    }

    @Override
    public String preferred() {
        return preferred;
    }
    
}
