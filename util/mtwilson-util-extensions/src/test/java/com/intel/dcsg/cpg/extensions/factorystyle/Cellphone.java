/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class Cellphone implements Telephone {

    @Override
    public String call(String number) {
        return String.format("Dialing %s wirelessly", number);
    }
    
}
