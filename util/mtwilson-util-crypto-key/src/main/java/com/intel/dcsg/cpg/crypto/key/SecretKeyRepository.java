/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

/**
 *
 * @author jbuhacoff
 */
public interface SecretKeyRepository {
    
    /**
     * 
     * @param keyId
     * @return the specified key or null if the key was not found
     */
    EncryptionKey find(byte[] keyId);
    
    /**
     * 
     * @param keyId
     * @return the specified key
     * @throws KeyNotFoundException if the specified key was not found
     */
    EncryptionKey findExisting(byte[] keyId) throws KeyNotFoundException;
}
