/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import javax.net.ssl.SSLException;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 *
 * @author jbuhacoff
 */
public class DenyAllHostnameVerifier extends AbstractVerifier {

    @Override
    public void verify(String string, String[] strings, String[] strings1) throws SSLException {
        throw new IllegalArgumentException("DENY-ALL");
    }
    
}
