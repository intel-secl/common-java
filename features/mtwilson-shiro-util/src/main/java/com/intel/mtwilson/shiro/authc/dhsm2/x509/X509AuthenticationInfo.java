/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 *
 * @author divyach1
 */
public class X509AuthenticationInfo implements AuthenticationInfo {
    private PrincipalCollection principals;
    private X509Filter credentials;

    @Override
    public PrincipalCollection getPrincipals() {
        return principals;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
    
    public void setPrincipals(PrincipalCollection principals) {
        this.principals = principals;
    }
    
    public void setCredentials(X509Filter credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return principals.toString();
    }

}
