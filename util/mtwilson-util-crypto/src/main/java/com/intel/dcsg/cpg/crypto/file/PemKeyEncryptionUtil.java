/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;

/**
 *
 * @author jbuhacoff
 */
public class PemKeyEncryptionUtil {
    public static PemKeyEncryption getEnvelope(Pem pem) {
        if( KeyEnvelope.isCompatible(pem) ) {
            return new KeyEnvelope(pem);
        }
        if( KeyEnvelopeV1.isCompatible(pem) ) {
            return new KeyEnvelopeV1(pem);
        }
        return null;
    }
    
}
