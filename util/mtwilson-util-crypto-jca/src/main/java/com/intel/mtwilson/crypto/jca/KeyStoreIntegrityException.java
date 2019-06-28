/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.security.KeyStoreException;

/**
 *
 * @author jbuhacoff
 */
public class KeyStoreIntegrityException extends KeyStoreException {

    public KeyStoreIntegrityException(String message) {
        super(message);
    }

    public KeyStoreIntegrityException(Throwable cause) {
        super(cause);
    }

    public KeyStoreIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

}
