/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface PemIntegrity {
    Pem getDocument();
    boolean isIntegrated();
    
    String getIntegrityKeyId();
    Integer getIntegrityKeyLength();
    String getIntegrityAlgorithm();
    List<String> getIntegrityManifest();
}
