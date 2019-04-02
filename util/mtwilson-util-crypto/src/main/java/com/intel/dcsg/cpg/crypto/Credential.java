/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public interface Credential {
    byte[] identity();
    byte[] signature(byte[] document) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;
    String algorithm();
}
