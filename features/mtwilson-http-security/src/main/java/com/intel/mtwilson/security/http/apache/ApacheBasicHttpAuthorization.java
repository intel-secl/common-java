/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.security.http.apache;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.auth.UsernamePasswordCredentials;

/**
 * @author jbuhacoff
 */
public class ApacheBasicHttpAuthorization implements ApacheHttpAuthorization {
    private UsernamePasswordCredentials credentials;
    public ApacheBasicHttpAuthorization(UsernamePasswordCredentials credentials) {
        this.credentials = credentials;
    }
    
    @Override
    public void addAuthorization(HttpRequest request) {
String login = String.format("%s:%s", credentials.getUserName(), credentials.getPassword());
String authorization = String.format("Basic %s", Base64.encodeBase64String(login.getBytes()));
                request.addHeader("Authorization", authorization);

    }

    @Override
    public void addAuthorization(HttpEntityEnclosingRequest request) {
String login = String.format("%s:%s", credentials.getUserName(), credentials.getPassword());
String authorization = String.format("Basic %s", Base64.encodeBase64String(login.getBytes()));
        request.addHeader("Authorization", authorization);
    }
    
}
