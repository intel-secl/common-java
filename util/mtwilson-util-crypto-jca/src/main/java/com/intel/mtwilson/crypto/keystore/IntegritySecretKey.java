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
public interface IntegritySecretKey {

    byte[] authenticate(byte[] message) throws NoSuchAlgorithmException, InvalidKeyException;
}
