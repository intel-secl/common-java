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
public class AcmeTelephoneFactory implements TelephoneFactory,Filter<String> {
    private String carrier = "Acme, Inc."; // pretend configuration value from somewhere
    
    @Override
    public Telephone create() {
        return new BrandedCellphone(carrier);
    }

    @Override
    public boolean accept(String item) {
        return "cell".equals(item);
    }
}
