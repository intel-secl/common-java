/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console.input;

import com.intel.dcsg.cpg.validation.InputModel;

/**
 *
 * @author jbuhacoff
 */
public class StringInput extends InputModel<String> {

    @Override
    protected String convert(String input) {
        return input;
    }
    

}
