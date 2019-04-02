/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.PublicKey;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface PublicKeyRepository {
    List<PublicKey> getPublicKeys();
}
