/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.shiro.authc.model.JwtBody;
import com.intel.mtwilson.shiro.authc.model.Permissions;
import com.intel.mtwilson.shiro.authc.model.Role;
import io.jsonwebtoken.Jwt;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;

import java.util.*;

/**
 *
 * @author arijit
 */
public class JWTAuthenticationToken implements AuthenticationToken {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JWTAuthenticationToken.class);
    private String token;

    public JWTAuthenticationToken(String token){
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return getKidFromToken();
    }

    @Override
    public Object getCredentials() {
        return getToken();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String[] getPermissionsFromToken(String applicationName) {
        JwtBody jwtBody = this.getJwtBodyFromToken();
        ArrayList<String> permissionsFromRole = new ArrayList<>();
        if(jwtBody != null && jwtBody.getPermissions() != null) {
            for (Permissions permissions : jwtBody.getPermissions()) {
                if (permissions.getService().equals(applicationName) && permissions.getRules() != null) {
                    permissionsFromRole.addAll(Arrays.asList(permissions.getRules()));
                }
            }
        }
        if (permissionsFromRole.size() == 0) {
            log.warn("No permission provided in JWT for service {}", applicationName);
        }
        String[] permissionsArray = new String[permissionsFromRole.size()];
        return permissionsFromRole.toArray(permissionsArray);
    }

    private String getKidFromToken() {
        ShiroUtil shiroUtil = new ShiroUtil();
        Jwt jwtClaims = shiroUtil.decodeTokenClaims(this.token);
        Object keyId = null;
        if (jwtClaims != null && jwtClaims.getHeader() != null && jwtClaims.getHeader().containsKey("kid")) {
            keyId = jwtClaims.getHeader().get("kid");
        }
        if (keyId != null){
            log.debug("JWT key ID: {}", keyId.toString());
            return keyId.toString();
        }
        return null;
    }

    private JwtBody getJwtBodyFromToken() {
        JwtBody jwtBody; //TODO: Jwt jwtClaims = shiroUtil.decodeTokenClaims(this.token);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] splitToken = this.token.split("\\.");
            //splitting the token adds unnecessary '\0' at the end of each splits which causes issue for mapper to parse the content
            //so it is necessary to replace the illegal characters
            jwtBody = mapper.readValue(new String( Base64.decode(splitToken[1])).replaceAll("\0+", "\"}").
                    replaceAll("\"{2,}", "\"").getBytes(), JwtBody.class);
        } catch (Exception exc) {
            log.error("getJwtBodyFromToken : {}", exc.getMessage());
            return null;
        }
        return jwtBody;
    }
}
