/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
