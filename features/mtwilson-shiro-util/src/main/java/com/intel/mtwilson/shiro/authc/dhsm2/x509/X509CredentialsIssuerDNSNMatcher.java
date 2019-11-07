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
