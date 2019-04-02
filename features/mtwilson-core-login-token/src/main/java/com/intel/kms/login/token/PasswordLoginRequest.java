/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import javax.ws.rs.FormParam;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="password_login_request")
public class PasswordLoginRequest {
    @FormParam("username")
    private String username;
    @FormParam("password")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}
