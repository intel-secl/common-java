/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.keystore;

/**
 *
 * @author jbuhacoff
 */
public interface CryptoForm {

    byte[] generateSalt();

    MasterKey deriveMasterKeyFromPasswordWithSalt(char[] password, byte[] salt);

    String toURI();
}
