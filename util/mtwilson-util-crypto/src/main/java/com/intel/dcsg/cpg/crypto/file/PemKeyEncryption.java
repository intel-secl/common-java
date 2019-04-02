/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.file;

/**
 *
 * @author jbuhacoff
 */
public interface PemKeyEncryption extends PemEncryption {
    
    String getContentKeyId();
    Integer getContentKeyLength(); // bits
    String getContentAlgorithm();
    String getContentMode();
    String getContentPaddingMode();
    
}
