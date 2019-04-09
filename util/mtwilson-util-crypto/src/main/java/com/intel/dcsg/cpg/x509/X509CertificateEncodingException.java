/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.x509;

import java.security.cert.X509Certificate;

/**
 * Thrown when an X509Certificate instance cannot be encoded to bytes 
 * via getEncoded()
 * 
 * @since 0.2
 * @author jbuhacoff
 */
public class X509CertificateEncodingException extends IllegalArgumentException {
    private X509Certificate certificate;
    public X509CertificateEncodingException(X509Certificate certificate) {
        super();
        this.certificate = certificate;
    }
    public X509CertificateEncodingException(Throwable cause, X509Certificate certificate) {
        super(cause);
        this.certificate = certificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }
}
