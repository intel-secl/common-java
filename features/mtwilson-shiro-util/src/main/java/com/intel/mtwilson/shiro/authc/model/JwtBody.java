/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.model;
/**
 *
 * @author arijit
 */
public class JwtBody {
    private Role[] roles;
    private String exp;
    private String iat;
    private String iss;
    private String sub;

    public Role[] getRoles() {
        return this.roles;
    }

    public void setRoles(Role[] roles) {
        this.roles = roles;
    }

    public String getExp() {
        return this.exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getIat() {
        return this.iat;
    }

    public void setIat(String iat) {
        this.iat = iat;
    }

    public String getIss() {
        return this.iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return this.sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}
