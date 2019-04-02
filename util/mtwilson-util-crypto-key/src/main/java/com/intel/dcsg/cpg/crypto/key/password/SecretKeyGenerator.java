/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

import javax.crypto.SecretKey;

/**
 *
 * @author jbuhacoff
 */
public interface SecretKeyGenerator {
    SecretKey generateSecretKey(String password, byte[] salt, PasswordProtection protection);
}
