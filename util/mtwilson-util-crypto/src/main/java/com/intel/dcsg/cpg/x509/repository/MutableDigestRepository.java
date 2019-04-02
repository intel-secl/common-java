/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.digest.Digest;

/**
 *
 * @author jbuhacoff
 */
public interface MutableDigestRepository extends DigestRepository {
    void addDigest(Digest digest);
}
