/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jackson;

/**
 *
 * @author jbuhacoff
 */
public class LowercaseWithHyphensStrategy extends LowercaseWithSeparatorStrategy {

    public LowercaseWithHyphensStrategy() {
        setSeparator('-');
    }

}
