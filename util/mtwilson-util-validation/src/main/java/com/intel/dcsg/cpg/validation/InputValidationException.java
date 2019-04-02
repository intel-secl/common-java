/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.validation;

/**
 *
 * @author jbuhacoff
 */
public class InputValidationException extends RuntimeException {
    public InputValidationException() {
        super();
    }
    public InputValidationException(String message) {
        super(message);
    }
    public InputValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    public InputValidationException(Throwable cause) {
        super(cause);
    }
}
