/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

import javax.crypto.SecretKey;

/**
 *
 * @author jbuhacoff
 */
public interface MutableSecretKeyRepository extends SecretKeyRepository {
    void add(EncryptionKey key);
}
