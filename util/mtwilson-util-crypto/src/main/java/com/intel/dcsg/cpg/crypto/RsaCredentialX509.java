/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class RsaCredentialX509 extends RsaCredential {
    private final X509Certificate certificate;
    
    /**
     * Initializes the RsaCredential using the provided private key and X509
     * certificate. The digest of the X509 certificate will be used as the
     * identity. Note this is not the same as the digest of the public key.
     * 
     * It is assumed that the public key in the certificate
     * correspond to the given private key.
     * 
     * @param privateKey
     * @param certificate
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException 
     */
    public RsaCredentialX509(PrivateKey privateKey, X509Certificate certificate) throws CryptographyException, CertificateEncodingException {
        super(privateKey, certificate.getEncoded()); // XXX RsaCredential(PrivateKey, byte[]) used to throw CertificateEncodingException but it was changed to throw CryptographyException instead.... for some reason the java compiler is still saying that super constructor in RsaCredential throws CertificateEncodingException when it clearly does't (right-click and go to declaration to see...)  dont' know where the former superclass is being cached so for now we're stuck throwing BOTH exceptions
        this.certificate = certificate;
    }
    
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

}
