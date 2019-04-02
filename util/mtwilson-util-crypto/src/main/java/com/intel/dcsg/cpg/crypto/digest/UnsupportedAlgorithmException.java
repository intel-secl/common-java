/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.digest;

/**
 *
 * @author jbuhacoff
 */
public class UnsupportedAlgorithmException extends UnsupportedOperationException {

    public UnsupportedAlgorithmException(String algorithm) {
        super(algorithm);
    }

    public UnsupportedAlgorithmException(String algorithm, Throwable cause) {
        super(algorithm, cause);
    }
    
}
