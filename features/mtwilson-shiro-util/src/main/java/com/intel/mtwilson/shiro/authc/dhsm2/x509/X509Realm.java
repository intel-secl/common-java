/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.codec.digest.DigestUtils ;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import com.intel.mtwilson.Folders;
import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.UsernameWithPermissions;



/**
 *
 * This class represents a Shiro Authentication Realm using X509 Certificate. It
 * accepts X509AuthenticationToken created from X509AuthenticationFilter or 
 * ForwardedX509AuthenticationFilter and validates against Issuer information
 * provided by X509AuthenticationInfo using the selected CredentialsMatcher. 
 * The AuthFilter, CredentialsMatcher and Realm to be used are specified in
 * shiro.ini
 * 
 * @author divyach1
 */
public class X509Realm extends AbstractX509Realm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509Realm.class);
    private String realmname;
    private String clientHost, clientCertSHA;
    private X509Certificate clientCert, issuerCert;
    private X500Principal issuerDN;
    
    public X509Realm() {
        realmname = "X509Realm";
    }
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof X509AuthenticationToken;
    }
    
    @Override
    /**
     * Represents Subject's authorization data such as permissions, roles
     * For now, X509 auth is only enabled for Key Transfer POST/Session POST
     * #TODO: Update permission string to be more limited  
    */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        /*if (pc == null) {
            throw new AuthorizationException("Principal must be provided");
        }*/
        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
        for (String realmName : pc.getRealmNames()) {
            log.debug("doGetAuthorizationInfo for realm: {}", realmName);
        }
        authzInfo.addStringPermission("*:*");
        return authzInfo;
    }
    
    @Override
    /**
     * Represents Subject's account information required for authc. Here, this 
     * represents Issuer CA information such as DN, serial
     * For testing, IssuerDN and Serial are hardcoded strings, which will later 
     * be retrieved from config info. 
     * #TODO: Issuer info retrieval from Jetty Truststore
     */
    protected X509AuthenticationInfo doGetX509AuthenticationInfo(X509AuthenticationToken token) 
            throws AuthenticationException {
        log.debug("In doGetX509AuthenticationInfo for realm: {}", realmname);
        try {
            log.debug("In doGetX509AuthenticationInfo for realm: {}", realmname);
            X509AuthenticationToken xToken = (X509AuthenticationToken) token;
            clientHost = xToken.getHost();
            clientCert = xToken.getX509Certificate();
            clientCertSHA = DigestUtils.sha256Hex(clientCert.getEncoded());
            
            //Create Subject Principals
            SimplePrincipalCollection principals = new SimplePrincipalCollection();
            log.debug("Adding principal host: {}", clientHost);
            principals.add(new String(clientHost), getName());
            log.debug("Adding principal Cert SHA: {}", clientCertSHA);
            principals.add(new String(clientCertSHA), getName());
            /**
             * TODO - Need to add Client SANs to principals
             */
            
            //Create Subject Credentials
            X509IssuerFilter credentials = new X509IssuerFilter();
            
            
            //Create Subject AuthInfo
            X509AuthenticationInfo info = new X509AuthenticationInfo();
            info.setPrincipals(principals);
            info.setCredentials(credentials);
            return info;
        } catch (Exception e) {
                log.debug("doGetAuthenticationInfo error", e);
                throw new AuthenticationException("Internal server error", e); 
        }
    }
}
