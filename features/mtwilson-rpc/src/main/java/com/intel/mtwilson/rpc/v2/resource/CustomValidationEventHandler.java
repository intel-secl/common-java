/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.rpc.v2.resource;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class CustomValidationEventHandler implements ValidationEventHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomValidationEventHandler.class);

    public boolean handleEvent(ValidationEvent event) {
        log.debug("CustomValidationEventHandler(): event severity : {}",event.getSeverity());
        log.debug("CustomValidationEventHandler(): event message : {}",event.getMessage());
        return false;
    }
}
