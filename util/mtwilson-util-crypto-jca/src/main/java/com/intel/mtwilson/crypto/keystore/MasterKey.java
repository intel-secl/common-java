/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
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
