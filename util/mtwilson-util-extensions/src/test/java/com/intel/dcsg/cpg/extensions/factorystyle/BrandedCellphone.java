/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class BrandedCellphone implements Telephone {
    private String carrier;
    public BrandedCellphone(String carrier) {
        this.carrier = carrier;
    }
    
    @Override
    public String call(String number) {
        return String.format("Dialing %s wirelessly on %s network", number, carrier);
    }
    
}
