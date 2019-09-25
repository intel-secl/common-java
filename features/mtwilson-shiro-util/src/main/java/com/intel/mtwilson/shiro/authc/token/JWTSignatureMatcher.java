/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.mtwilson.shiro.ShiroUtil;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
/**
 *
 * @author arijit
 */
public class JWTSignatureMatcher implements CredentialsMatcher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JWTSignatureMatcher.class);
    private String propertiesFile = null;
    private String trustedJwtCertDir = null;
    private String trustedCACertDir = null;

    public String getTrustedJwtCertDir() {
        return trustedJwtCertDir;
    }

    public void setTrustedJwtCertDir(String trustedJwtCertDir) {
        this.trustedJwtCertDir = trustedJwtCertDir;
    }

    public String getTrustedCACertDir() {
        return trustedCACertDir;
    }

    public void setTrustedCACertDir(String trustedCACertDir) {
        this.trustedCACertDir = trustedCACertDir;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        return new ShiroUtil().verifyJWTToken(token.getCredentials().toString(), getPropertiesFile());
    }
}
