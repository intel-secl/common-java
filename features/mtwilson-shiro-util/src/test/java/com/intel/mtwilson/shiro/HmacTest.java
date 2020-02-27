/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.keplerlake.authz.hmac.Message;
import com.intel.keplerlake.authz.hmac.MessageBuilder;
import com.intel.keplerlake.authz.hmac.Signature;
import com.intel.mtwilson.shiro.authc.hmac.HmacAuthenticationCredential;
import com.intel.mtwilson.shiro.authc.hmac.HmacAuthenticationInfo;
import com.intel.mtwilson.shiro.authc.hmac.HmacAuthenticationToken;
import com.intel.mtwilson.shiro.authc.hmac.HmacCredentialsMatcher;
import com.intel.mtwilson.shiro.authc.hmac.MemoryHmacRealm;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HmacTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacTest.class);

    private String createMessage() {
        Message message = MessageBuilder.factory()
                .authzParam("from", "client1234")
                .authzParam("realm", "example.com")
                .authzParam("headers", "Content-Type,Content-Length,Origin")
                .authzParam("query", "x,y")
                .queryParam("x", "0")
                .queryParam("y", "0")
                .authzParam("nonce", "AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok")
                .authzParam("digest_alg", "SHA256")
                .authzParam("timestamp", "2014-02-12T19:44:41-0800")
                .headerParam("Content-Type", "application/json")
                .headerParam("Content-Length", "2851")
                .headerParam("Origin", "example.com")
                .digestBase64("5a701ca2ee0a7892905e65a78ecec094492feca8482894aaa59e31437a54952c")
                .httpMethod("POST")
                .uri("https://example.com/path/to/resource")
                .build();
        return message.toString();
    }
    
    private SecretKey createSecretKey() {
        return new SecretKeySpec(RandomUtil.randomByteArray(16), "HMAC");
    }
    
    @Test
    public void testHmacCredentialsMatcher() throws NoSuchAlgorithmException, InvalidKeyException {
        // create a test user
        String username = "testuser1";
        SecretKey secretKey = new SecretKeySpec("changeit".getBytes(Charset.forName("UTF-8")), "HMAC");
        HashSet<String> permissions = new HashSet<>();
        permissions.add("test:test");
        MemoryHmacRealm.getDatabase().replace(secretKey, username, permissions);
//        MemoryHmacRealm.HmacRecord user = new MemoryHmacRealm.HmacRecord(secretKey, username, permissions);
        
        // prepare a signed message
        String messageText = createMessage();
        Signature signature = new Signature("SHA256", secretKey); // throws NoSuchAlgorithmException, InvalidKeyException
        byte[] messageBytes = messageText.getBytes(Charset.forName("UTF-8"));
        byte[] signatureBytes = signature.sign(messageBytes);
        
        HmacAuthenticationCredential credential = new HmacAuthenticationCredential(messageBytes, "SHA256", signatureBytes);
        HmacAuthenticationToken token = new HmacAuthenticationToken("testuser1", credential, "127.0.0.1");
        
        HmacCredentialsMatcher matcher = new HmacCredentialsMatcher();
        MemoryHmacRealm realm = new MemoryHmacRealm();
        realm.setCredentialsMatcher(matcher);
        
        HmacAuthenticationInfo info = (HmacAuthenticationInfo)realm.getAuthenticationInfo(token);
        
        // this is redundant - shiro already does this as part of getAuthenticationInfo above, and if it doesn't match an exception would have been thrown
        assertTrue(matcher.doCredentialsMatch(token, info)); 
    }
    
    @Test(expected=IncorrectCredentialsException.class)
    public void testHmacCredentialsMatcherWrongSecretKey() throws NoSuchAlgorithmException, InvalidKeyException {
        // create a test user
        String username = "client1234";
        SecretKey secretKey = createSecretKey();
        HashSet<String> permissions = new HashSet<>();
        permissions.add("test:test");
        MemoryHmacRealm.getDatabase().replace(secretKey, username, permissions);
//        MemoryHmacRealm.HmacRecord user = new MemoryHmacRealm.HmacRecord(secretKey, username, permissions);
        
        // prepare a signed message with WRONG SECRET KEY
        SecretKey secretKey2 = createSecretKey();
        String messageText = createMessage();
        Signature signature = new Signature("SHA256", secretKey2); // throws NoSuchAlgorithmException, InvalidKeyException
        byte[] messageBytes = messageText.getBytes(Charset.forName("UTF-8"));
        byte[] signatureBytes = signature.sign(messageBytes);
        
        HmacAuthenticationCredential credential = new HmacAuthenticationCredential(messageBytes, "SHA256", signatureBytes);
        HmacAuthenticationToken token = new HmacAuthenticationToken("client1234", credential, "127.0.0.1");
        
        HmacCredentialsMatcher matcher = new HmacCredentialsMatcher();
        MemoryHmacRealm realm = new MemoryHmacRealm();
        realm.setCredentialsMatcher(matcher);
        
        HmacAuthenticationInfo info = (HmacAuthenticationInfo)realm.getAuthenticationInfo(token);
        fail(); // shouldn't get here, shiro should throw IncorrectCredentialsException in previous line
        log.error("testHmacCredentialsMatcherWrongSecretKey passed authentication, but shouldn't have: {}", info);
    }
    
    @Test(expected=IncorrectCredentialsException.class)
    public void testHmacCredentialsMatcherReplay() throws NoSuchAlgorithmException, InvalidKeyException {
        // create a test user
        String username = "client1234";
        SecretKey secretKey = createSecretKey();
        HashSet<String> permissions = new HashSet<>();
        permissions.add("test:test");
        MemoryHmacRealm.getDatabase().replace(secretKey, username, permissions);
//        MemoryHmacRealm.HmacRecord user = new MemoryHmacRealm.HmacRecord(secretKey, username, permissions);
        
        // prepare a signed message
        String messageText = createMessage();
        Signature signature = new Signature("SHA256", secretKey); // throws NoSuchAlgorithmException, InvalidKeyException
        byte[] messageBytes = messageText.getBytes(Charset.forName("UTF-8"));
        byte[] signatureBytes = signature.sign(messageBytes);
        
        HmacAuthenticationCredential credential = new HmacAuthenticationCredential(messageBytes, "SHA256", signatureBytes);
        HmacAuthenticationToken token = new HmacAuthenticationToken("client1234", credential, "127.0.0.1");
        
        HmacCredentialsMatcher matcher = new HmacCredentialsMatcher();
        MemoryHmacRealm realm = new MemoryHmacRealm();
        realm.setCredentialsMatcher(matcher);
        
        // now authenticate twice - first time should work, second time should not work because of anti-replay protection
        HmacAuthenticationInfo info1 = (HmacAuthenticationInfo)realm.getAuthenticationInfo(token);
        assertNotNull(info1);

        HmacAuthenticationInfo info2 = (HmacAuthenticationInfo)realm.getAuthenticationInfo(token);
        fail(); // shouldn't get here, shiro should throw IncorrectCredentialsException in previous line
        log.error("testHmacCredentialsMatcherReplay passed authentication, but shouldn't have: {}", info2);
    }
    
        
}
