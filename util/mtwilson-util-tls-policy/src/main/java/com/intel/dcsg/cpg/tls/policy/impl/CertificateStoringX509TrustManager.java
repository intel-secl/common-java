/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Stores certificate chain from the last connection attempt. 
 * 
 * If the delegate constructor is used, the verification will be delegated
 * to that trust manager instance so that it's possible to chain the
 * certificate storing trust manager with any other implementation to 
 * actually check the server trust and then have the certificate chain
 * available in client code while connected.
 * 
 * @author jbuhacoff
 */
public class CertificateStoringX509TrustManager extends X509ExtendedServerTrustManager {
    private final X509ExtendedTrustManager delegate;
    private X509Certificate[] certificates = null;

    public CertificateStoringX509TrustManager() {
        this.delegate = null;
    }

    public CertificateStoringX509TrustManager(X509ExtendedTrustManager delegate) {
        this.delegate = delegate;
    }
    
    /**
     * 
     * @return certificate chain from last connection attempt, or null
     */
    public X509Certificate[] getStoredCertificates() {
        return certificates;
    }
    
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        certificates = chain;
        if( delegate != null ) {
            delegate.checkServerTrusted(chain, authType, socket);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine ssle) throws CertificateException {
        certificates = chain;
        if( delegate != null ) {
            delegate.checkServerTrusted(chain, authType, ssle);
        }
    }
    
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        certificates = chain;
        if( delegate != null ) {
            delegate.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
    
}
