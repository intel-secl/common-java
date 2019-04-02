/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import java.security.PrivateKey;

/**
 *
 * @author jbuhacoff
 */
public interface PrivateKeyFinder {
    PrivateKey find(String keyId);
}
