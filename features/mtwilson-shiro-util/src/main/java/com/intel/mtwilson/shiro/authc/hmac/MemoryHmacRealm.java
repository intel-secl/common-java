/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.hmac;

import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
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
 * 
 * @author jbuhacoff
 */
public class MemoryHmacRealm extends AuthorizingRealm {
    final private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryHmacRealm.class);
    final private static MemoryHmacDatabase database = new MemoryHmacDatabase();

    public static MemoryHmacDatabase getDatabase() {
        return database;
    }
    
    public MemoryHmacRealm() {
        super();
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
            Collection<Username> subjects = pc.byType(Username.class);
            for(Username username : subjects) {
                log.debug("doGetAuthorizationInfo for username: {}", username.getUsername());
                Set<String> permissions = database.findPermissionsByHmacPrincipal(username.getUsername());
                if( permissions == null ) {
                    log.debug("doGetAuthorizationInfo permissions not found: {}", username.getUsername());
                    continue;
                }
                log.debug("doGetAuthorizationInfo found permissions: {}", permissions);
                for(String permission : permissions ) {
                    log.debug("doGetAuthorizationInfo adding permision '{}'", permission);
                    authzInfo.addStringPermission(permission);
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
        HmacAuthenticationToken subjectToken = (HmacAuthenticationToken) token;
        String tokenPrincipal = subjectToken.getPrincipal();
        if (tokenPrincipal == null) {
            log.debug("doGetAuthenticationInfo null token principal");
            throw new AccountException("Token principal must be provided");
        }
        log.debug("doGetAuthenticationInfo for token {}", tokenPrincipal);
        HmacRecord hmacRecord;
        try {
            hmacRecord = database.findByHmacPrincipal(tokenPrincipal);
            if( hmacRecord == null ) {
                log.debug("doGetAuthenticationInfo token value not found in database: {}", tokenPrincipal);
                return null;
            }
            log.debug("doGetAuthenticationInfo found record for username {}", hmacRecord.getUsername());
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e); 
        }
        
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new Username(hmacRecord.getUsername()), getName());
        log.debug("Added Username principal: {}", hmacRecord.getUsername());

        HmacAuthenticationInfo info = new HmacAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(hmacRecord.getSecretKey());

        return info;
    }

    public static class MemoryHmacDatabase {
        final private HashMap<String,HmacRecord> userMap = new HashMap<>(); // tracks known users and their secret keys and permissions
        
        public void add(SecretKey secretKey, String username, Set<String> permissions) {
            synchronized(userMap) {
                HmacRecord existing = userMap.get(username);
                if( existing != null ) {
                    throw new IllegalArgumentException("Record already exists for this principal");
                }
                userMap.put(username, new HmacRecord(secretKey, username, permissions));
            }
        }

        public void replace(SecretKey secretKey, String username, Set<String> permissions) {
            synchronized(userMap) {
                userMap.remove(username);
                userMap.put(username, new HmacRecord(secretKey, username, permissions));
            }
        }
        
        public void remove(String username) {
            userMap.remove(username);
        }
        
        /**
         * 
         * @param value
         * @return the record (never null)
         * @throws IllegalArgumentException if not found
         */
        public HmacRecord getByHmacPrincipal(String value) {
            HmacRecord existing = findByHmacPrincipal(value);
            if( existing == null ) {
                throw new IllegalArgumentException("Record not found");
            }
            return existing;
        }
        
        /**
         * 
         * @param value
         * @return the record, or null if it was not found
         */
        public HmacRecord findByHmacPrincipal(String value) {
            return userMap.get(value);
        }
        
        public SecretKey findSecretKeyByHmacPrincipal(String value) {
            HmacRecord existing = userMap.get(value);
            if( existing == null ) { return null; }
            return existing.getSecretKey();
        }
        
        public Set<String> findPermissionsByHmacPrincipal(String value) {
            HmacRecord existing = userMap.get(value);
            if( existing == null ) { return null; }
            return existing.getPermissions();
        }


    }
    
    public static class HmacRecord {
        private final SecretKey secretKey; // the shared secret key to verify  messages
        private final String username; // set only if the token corresponds to an existing user
        private final Set<String> permissions; // each entry is in the format domain:action:selection format

        public HmacRecord(SecretKey secretKey, UsernameWithPermissions usernameWithPermissions) {
            this.secretKey = secretKey;
            this.username = usernameWithPermissions.getUsername();
            this.permissions = usernameWithPermissions.getPermissions();
        }
        public HmacRecord(SecretKey secretKey, String username, Set<String> permissions) {
            this.secretKey = secretKey;
            this.username = username;
            this.permissions = permissions;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public String getUsername() {
            return username;
        }
        
        public boolean isAnonymous() {
            return username == null || username.isEmpty();
        }
        

    }
}
