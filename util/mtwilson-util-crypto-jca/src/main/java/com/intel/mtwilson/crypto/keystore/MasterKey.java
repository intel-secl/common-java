/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.crypto.keystore;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author jbuhacoff
 */
public interface MasterKey {

    EncryptionSecretKey deriveEncryptionSecretKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException;

    IntegritySecretKey deriveIntegritySecretKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException;
}
