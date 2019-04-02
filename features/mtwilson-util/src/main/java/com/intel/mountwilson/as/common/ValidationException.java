/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mountwilson.as.common;
import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @author jbuhacoff
 */
public class ValidationException extends ASException {
    public ValidationException(String missingInput) {
        super(ErrorCode.AS_MISSING_INPUT, missingInput);
    }
}
