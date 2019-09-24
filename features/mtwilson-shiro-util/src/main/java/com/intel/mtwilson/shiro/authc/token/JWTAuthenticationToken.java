/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.shiro.ShiroUtil;
import com.intel.mtwilson.shiro.authc.model.JwtBody;
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

    public Set<String> getPermissionsFromToken(String applicationName) {
        JwtBody jwtBody = this.getJwtBodyFromToken();
        Set<String> permissions = new HashSet<>();
        if(jwtBody != null) {
            for (Role role : jwtBody.getRoles()) {
                if (role.getService().equals(applicationName)) {
                    Set<String> permissionsFromContext = getPermissionsFromContexts(getAllContexts(role.getContext()));
                    if (permissionsFromContext.size() > 0) {
                        permissions.addAll(getFormattedPermissions(applicationName, permissionsFromContext));
                    } else {
                        log.warn("No permission provided in JWT for service {}", applicationName);
                    }
                }
            }
        }
        return permissions;
    }

    /*
     * Splits contexts from full context string
     * Input : "context1=value;context2=value"
     * Output: [context1=value,context2=value]
     * */
    private Set<String> getAllContexts(String fullContext) {
        Set<String> contexts = new HashSet<>();
        if (fullContext != null && !fullContext.isEmpty()) {
            if (fullContext.contains(";")) {
                contexts.addAll(Arrays.asList(fullContext.split(";")));
            } else {
                contexts.add(fullContext);
            }
        }
        return contexts;
    }

    /*
    * Retrieves permissions from context
    * Input : Permissions=domain:action:selection,domain:action:selection
    * Output: [domain:action:selection,domain:action:selection]
    * */
    private Set<String> getPermissionsFromContexts(Set<String> contexts) {
        Set<String> permissions = new HashSet<>();
        for(String context: contexts) {
            if (context.contains("=")) {
                String[] contextMap = context.split("=");
                // contextName  = contextMap[0]
                if (contextMap.length == 2 && contextMap[0].toUpperCase().equals("PERMISSIONS")) {
                    String contextValue = contextMap[1];
                    if (contextValue.contains(",")) {
                        permissions.addAll(Arrays.asList(contextValue.split(",")));
                    } else {
                        permissions.add(contextValue);
                    }
                }
            }
        }
        return permissions;
    }

    /*
     * Formats permission
     * Input : Permissions=domain:action:selection,domain:action
     * Output: [domain:action:selection,domain:action:*]
     * */
    private Set<String> getFormattedPermissions(String applicationName, Set<String> permissionsFromContext) {
        Set<String> formattedPermissions = new HashSet<>();
        for (String permission : permissionsFromContext) {
            if (permission.split(":").length < 2 || permission.split(":").length > 3) {
                log.warn("Invalid permission provided in JWT for service {}", applicationName);
            } else if (permission.split(":").length == 3 && !permission.split(":")[2].isEmpty()) {
                formattedPermissions.add(permission);
            } else {
                formattedPermissions.add(permission + ":*");
            }
        }
        return formattedPermissions;
    }

    private String getKidFromToken() {
        ShiroUtil shiroUtil = new ShiroUtil();
        Jwt jwtClaims = shiroUtil.decodeTokenClaims(this.token);
        Object keyId = null;
        if (jwtClaims.getHeader() != null && jwtClaims.getHeader().containsKey("kid")) {
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
