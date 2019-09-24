/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

/**
 *
 * @author arijit
 */
public class JWTRealm extends AuthorizingRealm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JWTRealm.class);
    private String applicationName;
    private String tokenString;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() { return applicationName; }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> permissions = new JWTAuthenticationToken(tokenString).getPermissionsFromToken(applicationName);
        for (String permission : permissions) {
            authorizationInfo.addObjectPermission(new WildcardPermission(permission));
        }
        log.debug("Authorization info: {}", authorizationInfo.getObjectPermissions());
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JWTAuthenticationToken bearerToken = (JWTAuthenticationToken) token;
        this.tokenString = bearerToken.getToken();
        if (this.tokenString == null || this.tokenString.isEmpty()) {
            log.debug("doGetAuthenticationInfo() : null bearer token");
            throw new AccountException("JWT token must be provided");
        }
        return new SimpleAuthenticationInfo(bearerToken.getPrincipal(), bearerToken.getCredentials(), getName());
    }
}
