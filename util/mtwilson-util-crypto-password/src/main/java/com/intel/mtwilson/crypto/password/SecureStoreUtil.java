/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.crypto.password;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.util.Enumeration;

public  class SecureStoreUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecureStoreUtil.class); 
    public static void writeToStore(String keyStorePath, String keyStorePassword, String passwordKey,
                                    String alias, String password) throws Exception {

        byte[] salt = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);
        KeyStore.PasswordProtection keyStorePK = new KeyStore.PasswordProtection(passwordKey.toCharArray());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret =
                factory.generateSecret(new PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        13
                ));

        keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(
                generatedSecret), keyStorePK);

        FileOutputStream outputStream = new FileOutputStream(new File(keyStorePath));
        try {
            keyStore.store(outputStream, keyStorePassword.toCharArray());
		} finally {
			outputStream.close();
		}
    }

    public static String readFromStore(KeyStore keyStore, String passwordKey, String alias)
                                    throws Exception {
        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(passwordKey.toCharArray());

        KeyStore.SecretKeyEntry ske =
                (KeyStore.SecretKeyEntry)keyStore.getEntry(alias, keyStorePP);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec(
                ske.getSecretKey(),
                PBEKeySpec.class);

        return new String(keySpec.getPassword());

    }

    public static void createKeyStore(String pathToFile, String keystorePassword)
            throws Exception {
        KeyStore ks = KeyStore.getInstance("JCEKS");
        char[] pwdArray = keystorePassword.toCharArray();
        ks.load(null, pwdArray);
        try (FileOutputStream fos = new FileOutputStream(pathToFile)) {
            ks.store(fos, pwdArray);
        }

    }

    public static void deleteKeyStore(String pathToFile, String keystorePassword) {
        KeyStore keyStore = null;
        try {
            keyStore = loadKeyStore(pathToFile, keystorePassword);
        } catch (Exception e) {
        }
        Enumeration<String> aliases = null;
        try {
            if (keyStore != null) {
                aliases = keyStore.aliases();
            } else {
                log.info("No matching keystore found ");
            }
        } catch (KeyStoreException e) {
        }
        if(aliases != null)
        {
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            try {
                keyStore.deleteEntry(alias);
            } catch (KeyStoreException e) {
            }
        }
        }
        else {
           log.info("No matching aliases found in the keystore");
        }
    }

    public static void emptyKeyStore(String pathToFile) {
        try {
            Files.delete(Paths.get(pathToFile));
        } catch (IOException e) {
        }
    }

    public static KeyStore loadKeyStore(String pathToFile, String keystorePassword)
            throws Exception {
        File file = new File(pathToFile);
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        FileInputStream fis = new FileInputStream(file);
        try {
            keyStore.load(fis, keystorePassword.toCharArray());
        } finally {
            fis.close();
        }
        return keyStore;
    }
}
