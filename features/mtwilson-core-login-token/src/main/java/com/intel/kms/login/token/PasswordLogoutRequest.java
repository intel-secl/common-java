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
@JacksonXmlRootElement(localName="password_logout_request")
public class PasswordLogoutRequest {
    @FormParam("authorization_token")
    private String authorizationToken;

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }


    
}
