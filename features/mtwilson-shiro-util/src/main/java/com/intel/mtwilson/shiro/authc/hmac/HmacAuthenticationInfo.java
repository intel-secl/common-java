/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.hmac;

import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Encapsulates the HMAC secret key from the database
 * for a given user. This is used to verify the HMAC on an incoming
 * request.
 * 
 * @author jbuhacoff
 */
public class HmacAuthenticationInfo implements AuthenticationInfo {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacAuthenticationInfo.class);
    private PrincipalCollection principals;
    private SecretKey secretKey;
            
    public void setPrincipals(PrincipalCollection principals) {
        this.principals = principals;
    }
    
    @Override
    public PrincipalCollection getPrincipals() {
        return principals;
    }

    @Override
    public SecretKey getCredentials() {
        return secretKey;
    }
    
    public void setCredentials(SecretKey credentials) {
        this.secretKey = credentials;
    }
    
    @Override
    public int hashCode() {
        return principals == null ? 0 : principals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        log.debug("HmacAuthenticationInfo equals this: {} other: {}", this, obj);
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HmacAuthenticationInfo other = (HmacAuthenticationInfo) obj;
        if (!Objects.equals(this.principals, other.principals)) {
            return false;
        }
        if (!Objects.equals(this.secretKey, other.secretKey)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return principals.toString();
    }

}
