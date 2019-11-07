/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * Wraps any existing TlsPolicy to store the server certificates encountered
 * while validating a connection attempt. 
 * 
 * This is useful when clients need to do something with the server certificate
 * chain while connected to the server.
 * 
 * @author jbuhacoff
 */
public class CertificateStoringTlsPolicy implements TlsPolicy {
    private final TlsPolicy delegate;
    private final CertificateStoringX509TrustManager trustManager;

    public CertificateStoringTlsPolicy(TlsPolicy delegate) {
        this.delegate = delegate;
        this.trustManager = new CertificateStoringX509TrustManager(delegate.getTrustManager());
    }
    
    /**
     * 
     * @return the certificate chain from the last connection, or null
     */
    public X509Certificate[] getStoredCertificates() {
        return trustManager.getStoredCertificates();
    }
    
    /**
     * 
     * @return the wrapped tls policy
     */
    public TlsPolicy getDelegateTlsPolicy() {
        return delegate;
    }
    
    @Override
    public boolean providesConfidentiality() {
        return delegate.providesConfidentiality();
    }

    @Override
    public boolean providesAuthentication() {
        return delegate.providesAuthentication();
    }

    @Override
    public boolean providesIntegrity() {
        return delegate.providesIntegrity();
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return trustManager;
    }

    @Override
    public X509HostnameVerifier getHostnameVerifier() {
        return delegate.getHostnameVerifier();
    }

    @Override
    public ProtocolSelector getProtocolSelector() {
        return delegate.getProtocolSelector();
    }

}
