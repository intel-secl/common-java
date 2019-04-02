/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.GeneralSecurityException;

/**
 * Thrown when an input message fails an integrity check
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class MessageIntegrityException extends GeneralSecurityException {
    
    public MessageIntegrityException() {
        super();
    }
    public MessageIntegrityException(Throwable cause) {
        super(cause);
    }
    public MessageIntegrityException(String message) {
        super(message);
    }
    public MessageIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

}
