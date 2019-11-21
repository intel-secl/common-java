/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthorizingRealm;

public abstract class AbstractX509Realm
        extends AuthorizingRealm
{

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
            throws AuthenticationException
    {
        return doGetX509AuthenticationInfo( (X509AuthenticationToken) token );
    }

    protected abstract X509AuthenticationInfo doGetX509AuthenticationInfo( X509AuthenticationToken x509AuthenticationToken );

}
