/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.x509;

import com.intel.dcsg.cpg.io.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 *
 * @author jbuhacoff
 */
public class KeyStoreUtil {

    public static KeyStore createEmptyKeyStore() throws KeyStoreException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        return keystore;
    }
    
    public static KeyStore loadKeystore(File file, String password) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(new FileInputStream(file), password.toCharArray()); // throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException
        return keystore;
    }
    
    public static KeyStore loadKeystore(Resource resource, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(resource.getInputStream(), password.toCharArray()); // throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException
        return keystore;
    }
    
    public static void saveKeystore(KeyStore keystore, File file, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(new FileOutputStream(file), password.toCharArray());
    }

    public static void saveKeystore(KeyStore keystore, Resource resource, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(resource.getOutputStream(), password.toCharArray());
    }
    
}
