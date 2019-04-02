/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

import com.intel.mtwilson.pipe.Filter;

/**
 *
 * @author jbuhacoff
 */
public class TraditionalTelephoneFactory implements TelephoneFactory,Filter<String> {
    
    @Override
    public Telephone create() {
        return new Landline();
    }
    
    public Telephone create(String context) {
        if( "landline".equals(context) ) {
            return new Landline(); 
        }
        if( "cell".equals(context) ) {
            return new Cellphone();
        }
        throw new IllegalArgumentException("Unrecognized context");
    }

    @Override
    public boolean accept(String item) {
        return "landline".equals(item) || "cell".equals(item);
    }
}
