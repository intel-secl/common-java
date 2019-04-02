/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class CryptographyException extends Exception {
    public CryptographyException() {
        super();
    }
    public CryptographyException(Throwable cause) {
        super(cause);
    }
    public CryptographyException(String message) {
        super(message);
    }
    public CryptographyException(String message, Throwable cause) {
        super(message, cause);
    }
}
