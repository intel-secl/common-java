/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.hmac;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * The HmacAuthenticationFilter reads the entire request and 
 * checks the Authorization header for the HMAC authorization
 * scheme. If present, the Authorization header indicates 
 * which other headers to include in the signature. The 
 * HmacAuthenticationFilter recreates the "signed document"
 * and computes its digest. It then creates this
 * HmacAuthorizationToken with the username and signature provided
 * in the Authorization header as well as the reconstructed message that
 * was signed.
 * When the token is verified against the HmacAuthenticationInfo from
 * the database, the message signature is verified using the SecretKey
 * contained in the HmacAuthenticationInfo.
 * 
 * @author jbuhacoff
 */
public class HmacAuthenticationToken implements AuthenticationToken {
    private final String username;
    private final HmacAuthenticationCredential credential;
    private final String host;
    
    public HmacAuthenticationToken(String principal, HmacAuthenticationCredential credential, String host) {
        this.username = principal;
        this.credential = credential;
        this.host = host;
    }

    @Override
    public String getPrincipal() {
        return username;
    }

    @Override
    public HmacAuthenticationCredential getCredentials() {
        return credential;
    }

    public String getHost() {
        return host;
    }
    
    
    
}
