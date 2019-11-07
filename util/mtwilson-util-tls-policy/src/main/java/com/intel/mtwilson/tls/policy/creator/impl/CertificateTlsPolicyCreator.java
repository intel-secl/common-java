/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableCertificateRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
public class CertificateTlsPolicyCreator implements TlsPolicyCreator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateTlsPolicyCreator.class);
    
    @Override
    public CertificateTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "certificate".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                CertificateRepository repository = getCertificateRepository(tlsPolicyDescriptor);
                return new CertificateTlsPolicy(repository); //TlsPolicyBuilder.factory().strict(repository).build();
            }
            catch(CertificateException | KeyManagementException e) {
                throw new IllegalArgumentException("Cannot create certificate policy from given repository", e);
            }
        }
        return null;
    }
    public static class CertificateMetadata {
        public String encoding; // base64
    }
    
   
    private CertificateRepository getCertificateRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CertificateException, KeyManagementException {
        HashSetMutableCertificateRepository repository = new HashSetMutableCertificateRepository();
        if( "certificate".equals(tlsPolicyDescriptor.getPolicyType()) && tlsPolicyDescriptor.getData() != null  ) {
            if( tlsPolicyDescriptor.getData() == null || tlsPolicyDescriptor.getData().isEmpty()  ) {
                throw new IllegalArgumentException("TLS policy descriptor does not contain any certificates");
            }
            ByteArrayCodec codec = getCodecForTlsPolicyDescriptor(tlsPolicyDescriptor);
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates certificates but does not declare certificate encoding");
            }
            for(String certificateBase64 : tlsPolicyDescriptor.getData()) {
                X509Certificate certificate = X509Util.decodeDerCertificate(codec.decode(certificateBase64));
                repository.addCertificate(certificate);
            }
            return repository;
        }
        return null;
    }    
    
    protected ByteArrayCodec getCodecForTlsPolicyDescriptor(TlsPolicyDescriptor tlsPolicyDescriptor) {
        ByteArrayCodec codec;
        CertificateMetadata meta = getCertificateMetadata(tlsPolicyDescriptor);
        if( meta.encoding == null ) {
            // attempt auto-detection based on first certificate
            String sample = TlsPolicyFactoryUtil.getFirst(tlsPolicyDescriptor.getData());
            meta.encoding = TlsPolicyFactoryUtil.guessEncodingForData(sample);
            log.debug("Guessing codec {} for sample data {}", meta.encoding, sample);
        }
        codec = TlsPolicyFactoryUtil.getCodecByName(meta.encoding); // safe because if input is null return value will be null
        log.debug("Codec {} for cerrtificate encoding {}", (codec==null?"null":codec.getClass().getName()), meta.encoding);
        return codec;
    }
    
    
    /**
     * 
     * @param tlsPolicyDescriptor
     * @return an instance of CertificateDigestMetadata, but some fields may be null if they were not included in the descriptor's meta section
     */
    public static CertificateMetadata getCertificateMetadata(TlsPolicyDescriptor tlsPolicyDescriptor) {
        CertificateMetadata metadata = new CertificateMetadata();
        if( tlsPolicyDescriptor.getMeta() == null ) {
            return metadata;
        }
        if(tlsPolicyDescriptor.getMeta() == null) {
            throw new IllegalArgumentException("TLS policy descriptor metadata cannot be null.");
        }
        String encoding = tlsPolicyDescriptor.getMeta().get("encoding");
        if( encoding != null && !encoding.isEmpty() ) {
            metadata.encoding = encoding;
        }
        return metadata;
    }
    
    
    public static CertificateTlsPolicy withCertificates(Collection<X509Certificate> certificates) {
        HashSetMutableCertificateRepository repository = new HashSetMutableCertificateRepository(certificates);
        CertificateTlsPolicy certificateTlsPolicy = new CertificateTlsPolicy(repository);
        return certificateTlsPolicy;
    }
}
