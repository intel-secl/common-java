/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationToken;

import org.apache.shiro.web.filter.authc.AuthenticatingFilter;


public class X509AuthenticationFilter
        extends AuthenticatingFilter
{

    final private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
            throws Exception
    {
        return executeLogin( request, response );
    }

    @Override
    protected AuthenticationToken createToken( ServletRequest request, ServletResponse response )
            throws Exception
    {
        log.debug( "In X509AuthFilter.createToken()" );
        HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        X509Certificate[] clientCertChain = ( X509Certificate[] ) httpRequest.getAttribute( "javax.servlet.request.X509Certificate" );
        if( log.isDebugEnabled() )
        {
            log.debug( "X509AuthFilter.createToken() cert chain is {}", Arrays.toString( clientCertChain ) );
            log.debug( "X509AuthFilter.createToken() host is {}", getHost( request ) );
        }
        if ( clientCertChain == null || clientCertChain.length < 1 ) {
            throw new ShiroException( "Request do not contain any X509Certificate" );
        }
        return new X509AuthenticationToken( clientCertChain, getHost( request ) );
    }

}
