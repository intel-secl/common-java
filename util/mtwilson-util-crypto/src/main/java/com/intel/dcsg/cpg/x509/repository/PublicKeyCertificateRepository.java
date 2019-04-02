/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * An adapter to provide the PublicKeyRepository interface for an 
 * underlying CertificateRepository
 * 
 * @author jbuhacoff
 */
public class PublicKeyCertificateRepository implements PublicKeyRepository {
    private CertificateRepository repository;

    public PublicKeyCertificateRepository(CertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PublicKey> getPublicKeys() {
        ArrayList<PublicKey> publicKeys = new ArrayList<>();
        for(X509Certificate certificate : repository.getCertificates()) {
            publicKeys.add(certificate.getPublicKey());
        }
        return publicKeys;
    }
    
    
}
