/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util;

/**
 *
 * @author jbuhacoff
 */
public interface DataCipher {
    String encryptString(String plaintext);
    String decryptString(String ciphertext);
}
