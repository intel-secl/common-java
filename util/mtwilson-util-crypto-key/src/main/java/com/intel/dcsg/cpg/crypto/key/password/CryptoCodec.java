/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

/**
 *
 * @author jbuhacoff
 */
public interface CryptoCodec {
    byte[] encrypt(byte[] plaintext);
    byte[] decrypt(byte[] ciphertext);
}
