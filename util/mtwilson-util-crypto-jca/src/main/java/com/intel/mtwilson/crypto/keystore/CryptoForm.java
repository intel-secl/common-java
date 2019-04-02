/*
 * Copyright 2016 Intel Corporation. All rights reserved.
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
