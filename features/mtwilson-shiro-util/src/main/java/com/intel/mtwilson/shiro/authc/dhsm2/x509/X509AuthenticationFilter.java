/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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