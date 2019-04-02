/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 * Notice this one doesn't implement TelephoneFactory or Filter directly , but
 * it overrides both create(object) and accept(object)
 * @author jbuhacoff
 */
public class LandlineTelephoneFactory extends TraditionalTelephoneFactory {
    @Override
    public Telephone create(String context) {
        return new Landline();
    }
    @Override
    public boolean accept(String context) {
        return "landline".equals(context);
    }
}
