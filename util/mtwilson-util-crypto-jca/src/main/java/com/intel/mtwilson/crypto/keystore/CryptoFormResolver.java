/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.keystore;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
public class CryptoFormResolver {

    public CryptoForm getCryptoForm(String uri) throws NoSuchAlgorithmException {
        if (KeystoreCryptoForm.KEYSTORE_CRYPTOFORM_URI.equals(uri)) {
            // A KeyStore implementation using PBKDF2, HMAC-SHA256, and AES-CBC with PKCS #7 padding
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("masterKeyLengthBits", "128");
            parameters.put("iterations", "10000");
            return new KeystoreCryptoForm(parameters);
        }
        return null;
    }
}
