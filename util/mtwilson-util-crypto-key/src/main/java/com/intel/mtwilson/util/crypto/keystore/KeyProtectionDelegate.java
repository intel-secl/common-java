/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.keystore;

/**
 *
 * @author jbuhacoff
 */
public interface KeyProtectionDelegate {
    char[] getPassword(String keyId);
}
