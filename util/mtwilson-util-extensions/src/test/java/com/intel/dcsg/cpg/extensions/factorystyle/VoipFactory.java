/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class VoipFactory {
    public Voip create() {
        // pretend the "provider" was configured somewhere, like with My.configuration().getProvider()
        return new Voip("Example Inc.");
    }
}
