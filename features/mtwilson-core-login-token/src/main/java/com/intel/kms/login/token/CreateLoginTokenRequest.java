/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class CreateLoginTokenRequest {
    private ArrayList<LoginTokenAttributes> data = new ArrayList<>();

    public CreateLoginTokenRequest() {
    }

    
    public ArrayList<LoginTokenAttributes> getData() {
        return data;
    }

}
