/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import javax.security.auth.x500.X500Principal;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

public abstract class AbstractX509CredentialsMatcher
        implements CredentialsMatcher
{

    @Override
    public final boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        return doX509CredentialsMatch( ( X509AuthenticationToken ) token, ( X509AuthenticationInfo ) info );
    }

    public abstract boolean doX509CredentialsMatch( X509AuthenticationToken token, X509AuthenticationInfo info );

    protected final String toString( X500Principal dn )
    {
        return dn.getName( X500Principal.CANONICAL );
    }

    protected final boolean doEquals( X500Principal one, X500Principal other )
    {
        return toString( one ).equals( toString( other ) );
    }

}
