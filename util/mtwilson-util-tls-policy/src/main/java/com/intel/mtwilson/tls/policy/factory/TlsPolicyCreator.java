/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

/**
 *
 * @author jbuhacoff
 */
public interface TlsPolicyCreator {
    /**
     * 
     * @param tlsPolicyDescriptor
     * @return an implementation of TlsPolicy or null if the given TlsPolicyDescriptor is not supported by this creator
     * @throws IllegalArgumentException if the TlsPolicyDescriptor is supported but has invalid data
     */
    TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor);
}
