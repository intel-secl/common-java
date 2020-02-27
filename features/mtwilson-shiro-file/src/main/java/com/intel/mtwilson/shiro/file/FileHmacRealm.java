/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

import com.intel.mtwilson.Folders;
import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.shiro.authc.hmac.HmacAuthenticationInfo;
import com.intel.mtwilson.shiro.authc.hmac.HmacAuthenticationToken;
import com.intel.mtwilson.shiro.file.model.UserHmacSecretKey;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import com.intel.mtwilson.util.crypto.keystore.SecretKeyStore;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.crypto.SecretKey;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * How to configure this in shiro.ini:
 *
 * <pre>
 * fileHmacRealm=com.intel.mtwilson.shiro.file.FilePasswordRealm
 * fileHmacRealm.userKeyStoreType=JCEKS
 * fileHmacRealm.userKeyStorePassword=changeit
 * fileHmacRealm.credentialsMatcher=$hmacMatcher
 * fileHmacRealm.authorizationCachingEnabled=false
 * </pre>
 *
 * The $hmacMatcher variable must be declared above that block:
 *
 * <pre>
 * hmacMatcher=com.intel.mtwilson.shiro.authc.hmac.HmacCredentialsMatcher
 * </pre>
 *
 * @author jbuhacoff
 */
public class FileHmacRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileHmacRealm.class);

    private String userKeyStoreFilePath;
    private String userKeyStoreType; // jceks or mtwks
    private char[] userKeyStorePassword; // keystore password, also used as password for each of the entries
    private String permissionFilePath;

    public FileHmacRealm() {
        userKeyStoreFilePath = Folders.configuration() + File.separator + "user-hmac.jceks"; // todo: change to mtwks
        permissionFilePath = Folders.configuration() + File.separator + "permissions.txt";
    }

    public void setUserKeyStoreFilePath(String userKeyStoreFilePath) {
        this.userKeyStoreFilePath = userKeyStoreFilePath;
    }

    public void setPermissionFilePath(String permissionFilePath) {
        this.permissionFilePath = permissionFilePath;
    }

    public void setUserKeyStoreType(String userKeyStoreType) {
        this.userKeyStoreType = userKeyStoreType;
    }

    public String getUserKeyStoreType() {
        return userKeyStoreType;
    }

    public void setUserKeyStorePassword(String userKeyStorePassword) {
        this.userKeyStorePassword = userKeyStorePassword.toCharArray();
    }

    public String getUserKeyStorePassword() {
        return String.valueOf(userKeyStorePassword);
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof HmacAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        if (pc == null) {
            throw new AuthorizationException("Principal must be provided");
        }
        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
        for (String realmName : pc.getRealmNames()) {
            log.debug("doGetAuthorizationInfo for realm: {}", realmName);
        }
        try {
            PermissionDAO dao = new PermissionDAO(new File(permissionFilePath));
            Collection<Username> usernames = pc.byType(Username.class);
            for (Username username : usernames) {
                log.debug("doGetAuthorizationInfo for username: {}", username.getUsername());
                List<UserPermission> permissions = dao.getPermissions(username.getUsername());
                for (UserPermission permission : permissions) {
                    authzInfo.addStringPermission(permission.toString());
                }
            }
        } catch (Exception e) {
            log.debug("doGetAuthorizationInfo error", e);
            throw new AuthenticationException("Internal server error", e);
        }

        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        HmacAuthenticationToken hmacAuthorizationToken = (HmacAuthenticationToken) token;
        String username = hmacAuthorizationToken.getPrincipal();
        if (username == null) {
            log.debug("doGetAuthenticationInfo null username");
            throw new AccountException("Username must be provided");
        }
        log.debug("doGetAuthenticationInfo for username {}", username);
        UsernameWithPermissions usernameWithPermissions = null;
        UserHmacSecretKey userHmacSecretKey = null;
        SecretKey secretKey = null;
        try {
            // load user entry from keystore
            SecretKeyStore keystore = new SecretKeyStore(userKeyStoreType, new File(userKeyStoreFilePath), userKeyStorePassword);
            if (!keystore.aliases().contains(username)) {
                return null;
            }
            secretKey = keystore.get(username);
            userHmacSecretKey = new UserHmacSecretKey(username, "HMAC", "SHA256", secretKey.getEncoded());
            PermissionDAO dao = new PermissionDAO(new File(permissionFilePath));
            List<UserPermission> userPermissions = dao.getPermissions(username);
            HashSet<String> stringPermissions = new HashSet<>();
            for (UserPermission p : userPermissions) {
                stringPermissions.add(p.toString());
            }
            usernameWithPermissions = new UsernameWithPermissions(username, stringPermissions);

        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e);
        }
        log.debug("doGetAuthenticationInfo found user {}", userHmacSecretKey.getUsername());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(usernameWithPermissions, getName());
        principals.add(userHmacSecretKey, getName());

        HmacAuthenticationInfo info = new HmacAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(secretKey);

        return info;
    }

}
