/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.mtwilson.shiro.HttpAuthenticationFilter;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author arijit
 */
public class JWTTokenAuthenticationFilter extends HttpAuthenticationFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JWTTokenAuthenticationFilter.class);
    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final String AUTHENTICATION_SCHEME_UC = "BEARER";

    public JWTTokenAuthenticationFilter() {
        super();
        setAuthenticationScheme(AUTHENTICATION_SCHEME);
    }

    @Override
    protected boolean isAuthenticationRequest(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return isHeaderAuthenticationRequest(httpRequest);
    }

    private boolean isHeaderAuthenticationRequest(HttpServletRequest httpRequest) {
        String authorizationHeaderName = getAuthorizationHeaderName();
        if (authorizationHeaderName != null) {
            String authorizationHeaderValue = httpRequest.getHeader(authorizationHeaderName);
            if (authorizationHeaderValue != null) {
                return authorizationHeaderValue.toUpperCase().startsWith(AUTHENTICATION_SCHEME_UC + " ");
            }
        }
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        log.debug("Create Token");
        String bearerTokenString = getTokenFromHeader((HttpServletRequest)request);
        return new JWTAuthenticationToken(bearerTokenString);
    }

    private String getTokenFromHeader(HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader(getAuthorizationHeaderName());
        // splitting on spaces should yield "Token" followed by a literal
        if (authorizationHeader != null) {
            String[] terms = authorizationHeader.split(" ");
            if (terms.length == 2 && AUTHENTICATION_SCHEME_UC.equals(terms[0].toUpperCase()) && !terms[1].isEmpty()) {
                return terms[1];
            } else {
                log.error("Authorization header does not contain bearer token");
            }
        }
        return null;
    }
}

