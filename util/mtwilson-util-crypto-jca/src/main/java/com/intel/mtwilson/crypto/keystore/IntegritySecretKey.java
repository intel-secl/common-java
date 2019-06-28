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
public interface IntegritySecretKey {

    byte[] authenticate(byte[] message) throws NoSuchAlgorithmException, InvalidKeyException;
}
