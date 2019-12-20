/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.mtwilson.crypto.jca.MtWilsonProvider;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class PasswordKeyStoreTest {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordKeyStoreTest.class);

        @BeforeClass
        public static void initCryptoProvider() {
            Security.addProvider(new MtWilsonProvider());
        }
        @Test
        public void testCreatePasswordKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
            Password keystorePassword = new Password("changeit");
            Resource resource = new ByteArrayResource();
            try(PasswordKeyStore keystore = new PasswordKeyStore("MTWKS", resource, keystorePassword)) {
                keystore.set("alias1", new Password("password1"));
            }
            try(PasswordKeyStore keystore = new PasswordKeyStore("MTWKS", resource, keystorePassword)) {
                Password retrieved = keystore.get("alias1");
                assertArrayEquals("password1".toCharArray(), retrieved.toCharArray());
            }
            
        }
}
