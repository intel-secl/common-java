/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.hmac;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.keplerlake.authz.hmac.KeyValue;
import com.intel.keplerlake.authz.hmac.Message;
import com.intel.keplerlake.authz.hmac.Signature;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 * Verifies the signature of an incoming HmacAuthenticationToken using a known
 * SecretKey from HmacAuthenticationInfo.
 *
 * AuthenticationToken must be an instance of HmacAuthenticationToken.
 * AuthenticationInfo must be an instance of HmacAuthenticationInfo.
 * 
 * 
 * @author jbuhacoff
 */
public class HmacCredentialsMatcher implements CredentialsMatcher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacCredentialsMatcher.class);
    private static final MemoryNonceTracker nonceTracker = new MemoryNonceTracker(); // tracks nonces seen with their request date, to prevent replay; the server defines a "not before" date to which incoming requests are compared, they must have a timestamp equal to or after the "not before" date;  the server will periodically delete tracked nonces that are before the "not before" 

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        log.debug("doCredentialsMatch");
        if( !(token instanceof HmacAuthenticationToken) ) {
            log.debug("doCredentialsMatch incorrect token class: {}", token.getClass().getName());
            return false;
        }
        if( !(info instanceof HmacAuthenticationInfo) ) {
            log.debug("doCredentialsMatch incorrect info class: {}", info.getClass().getName());
            return false;
        }
        HmacAuthenticationToken hmacToken = (HmacAuthenticationToken)token;
        HmacAuthenticationInfo hmacInfo = (HmacAuthenticationInfo)info;
        
        HmacAuthenticationCredential credential = hmacToken.getCredentials();
        String digestAlg = credential.getDigestAlg();
        SecretKey secretKey = hmacInfo.getCredentials();
        try {
            log.debug("doCredentialsMatch verifying signature");
            log.debug("doCredentialsMatch message base64: {}", Base64.encodeBase64String(credential.getMessage()));
            
            // recompute the hmac using same input as the client
            Signature signature = new Signature(digestAlg, secretKey);
            byte[] signatureBytes = signature.sign(credential.getMessage());
            log.debug("doCredentialsMatch received signature: {}", Base64.encodeBase64String(credential.getSignature()));
            log.debug("doCredentialsMatch computed signature: {}", Base64.encodeBase64String(signatureBytes));
            
            if (Arrays.equals(signatureBytes, credential.getSignature())) {
                log.debug("doCredentialsMatch verified signature");

                // we only keep track of nonces from the last 24 hours
                Date now = new Date();
                Date notBefore = new Date(now.getTime() - TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS));
                nonceTracker.clearAllNonceBefore(notBefore);
                
                // check if the nonce has been seen before
                String nonceBase64 = null;
                Message message = Message.parse(new String(hmacToken.getCredentials().getMessage(), Charset.forName("UTF-8")));
                for(KeyValue kv : message.getAuthorizationParameters().list()) {
                    if( kv.getKey().equalsIgnoreCase("nonce")) {
                        nonceBase64 = kv.getValue();
                    }
                }
                
                Date date = nonceTracker.getNonceDate(nonceBase64);
                if( date != null ) {
                    log.debug("doCredentialsMatch nonce {} already seen on: {}", nonceBase64, Iso8601Date.format(date));
                    return false;
                }

                // message was valid, so record the nonce to protect against future replay
                nonceTracker.track(nonceBase64);
                
                return true;
            }
            log.error("doCredentialsMatch invalid signature");
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("doCredentialsMatch cannot verify credentials: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }
    
    public static class MemoryNonceTracker {
        private final HashMap<String,Date> nonceMap = new HashMap<>(); // tracks nonces seen with their request date, to prevent replay; the server defines a "not before" date to which incoming requests are compared, they must have a timestamp equal to or after the "not before" date;  the server will periodically delete tracked nonces that are before the "not before" 
         public void track(String nonce) {
            nonceMap.put(nonce, new Date());
        }
        
        /**
         * 
         * @param nonce
         * @return the date this nonce was seen, or null if this nonce has not been seen
         */
        public Date getNonceDate(String nonce) {
            return nonceMap.get(nonce);
        }
        
        /**
         * Clears all nonces seen before the "notBefore" date. The server
         * will call this method when it advances the "window" for incoming
         * nonces.
         * 
         * @param notBefore 
         */
        public void clearAllNonceBefore(Date notBefore) {
            ArrayList<String> toRemove = new ArrayList<>();
            for(Map.Entry<String,Date> entry : nonceMap.entrySet()) {
                if( entry.getValue().before(notBefore) ) {
                    toRemove.add(entry.getKey());
                }
            }
            for(String nonce : toRemove) {
                nonceMap.remove(nonce);
            }
        }           
    }
}
