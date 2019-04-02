/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.crypto.keystore;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author jbuhacoff
 */
public interface EncryptionSecretKey {

    byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException;

    byte[] decrypt(byte[] ciphertext) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException;
}
