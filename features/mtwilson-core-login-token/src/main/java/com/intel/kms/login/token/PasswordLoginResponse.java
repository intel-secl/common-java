/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.kms.login.token;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="password_login_response")
public class PasswordLoginResponse extends LoginTokenResponse {
    public PasswordLoginResponse() {
        super();
    }
}
