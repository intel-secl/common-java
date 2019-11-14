/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.model;

public class Permissions {
    private String service;
    private String context;
    private String[] rules;

    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String[] getRules() {
        return this.rules;
    }

    public void setRules(String[] rules) {
        this.rules = rules;
    }
}
