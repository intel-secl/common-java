/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper around java.security.KeyStore that attempts to simplify its use.
 * 
 * @author jbuhacoff
 */
public abstract class AbstractKeyStore implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractKeyStore.class);
    private final String keystoreType;
    private final Resource keystoreResource;
    private final char[] keystorePassword;
    private KeyStore keystore;
    private boolean modified = false;

    /**
     * 
     * @param keystoreType to use as argument to {@code KeyStore.getInstance()}
     * @param keystoreFile
     * @param keystorePassword
     * @throws KeyStoreException
     * @throws IOException 
     */
    public AbstractKeyStore(String keystoreType, File keystoreFile, char[] keystorePassword) throws KeyStoreException, IOException {
        this.keystoreType = keystoreType;
        this.keystoreResource = new FileResource(keystoreFile);
        this.keystorePassword = keystorePassword;
        open();
    }
    
    public AbstractKeyStore(String keystoreType, Resource keystoreResource, char[] keystorePassword) throws KeyStoreException, IOException {
        this.keystoreType = keystoreType;
        this.keystoreResource = keystoreResource;
        this.keystorePassword = keystorePassword;
        open();
    }

    public AbstractKeyStore(String keystoreType, Resource keystoreResource, Password keystorePassword) throws KeyStoreException, IOException {
        this.keystoreType = keystoreType;
        this.keystoreResource = keystoreResource;
        this.keystorePassword = keystorePassword.toCharArray();
        open();
    }
    
    private void open() throws KeyStoreException, IOException {
        this.keystore = KeyStore.getInstance(keystoreType);
        if( keystoreResource == null ) {
            throw new IllegalArgumentException("Keystore resource not specified");
        }
        try(InputStream in = keystoreResource.getInputStream()) {
            keystore.load(in, keystorePassword); // input stream will be null when file is not found or if it's an empty ByteArrayResource
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException("Cannot open keystore", e);
        }
    }

    /**
     * Writes the keystore content to the file or resource only if the
     * keystore was modified since it was opened. To force writing the
     * keystore call {@code modified()} before calling {@code close()}
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        // save the keystore file only if there have been changes
        if( isModified() ) {
            try (OutputStream out = keystoreResource.getOutputStream()) {
                keystore.store(out, keystorePassword);
            } catch (GeneralSecurityException e) {
                throw new IOException("Cannot close keystore", e);
            }
        }
    }

    public boolean isEmpty() throws KeyStoreException {
        return keystore.size() == 0;
    }

    public boolean contains(String alias) throws KeyStoreException {
        return keystore.containsAlias(alias)/* && keystore.isKeyEntry(alias)*/;
    }


    public void remove(String alias) throws KeyStoreException {
        keystore.deleteEntry(alias);
        modified();
    }
    
    /**
     * 
     * @return the underlying {@code java.security.KeyStore} object
     */
    public KeyStore keystore() {
        return keystore;
    }
    
    public List<String> aliases() throws KeyStoreException {
        return Collections.list(keystore.aliases());
    }

    /**
     * Call to indicate that the keystore has been modified and should be
     * written out when {@code close()} is called. 
     */
    public void modified() {
        modified = true;
    }
    
    /**
     * Note that is you obtain the underlying keystore using {@code keystore()}
     * this instance will not be able to track if any changes were made there,
     * so call {@code modified()} to hint that the underlying keystore was
     * edited. 
     * @return true if any entries were added, replaced, or deleted
     */
    public boolean isModified() {
        return modified;
    }
}
