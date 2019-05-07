/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util;

import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Aes128DataCipher implements DataCipher {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Aes128 cipher;

    public Aes128DataCipher(Aes128 cipher) {
        this.cipher = cipher;
    }

    @Override
    public String encryptString(String plaintext) {
        try {
            return cipher.encryptString(plaintext);
        } catch (CryptographyException e) {
            log.error("Failed to encrypt data", e);
            return null;
        }
    }

    @Override
    public String decryptString(String ciphertext) {
        try {
            return cipher.decryptString(ciphertext);
        } catch (CryptographyException e) {
            log.error("Failed to decrypt data", e);
            return null;
        }
    }
}
