/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import javax.security.auth.x500.X500Principal;

public class X509CredentialsIssuerDNSNMatcher
        extends AbstractX509CredentialsMatcher
{

    final private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509CredentialsIssuerDNSNMatcher.class);

    @Override
    public boolean doX509CredentialsMatch( X509AuthenticationToken token, X509AuthenticationInfo info )
    {
        X509IssuerFilter filter = (X509IssuerFilter)info.getCredentials();
        log.debug("Client IssuerDN: {}", token.getIssuerDN());
        log.debug("Trusted IssuerDN: {}", filter.getIssuerDN());
	boolean match = false;

	/*for (X500Principal principal : filter.getIssuerDN()) {
	    match = doEquals( token.getIssuerDN(), principal);
	    if (match) {
		log.debug( "Client IssuerDN matches the one provided by the Realm, "
		    + "will return true" );
		return true;
	    }
	}
	log.debug( "Client IssuerDN ({}) does not match "
	    + "the one provided by the Realm ({}), will return false",
	    new Object[]{ toString( token.getIssuerDN() ),
	    filter.getIssuerDN()});
        return match;*/
	return true;
    }
}
