/*
 * Copyright (C) 2019 Intel Corporation
 *  SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.crypto.Sha384Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.AnyProtocolSelector;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authc.AuthenticationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @author ddhawal
 */
public class CMSRootCaDownloader {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMSRootCaDownloader.class);

    private static final String CMS_TLS_CERT_SHA384 = "cms.tls.cert.sha384";

    public static X509Certificate downloadCmsCaCert(String cmsBaseUrl) throws Exception {
        String tmpKeystorePath = Folders.configuration()+ File.separator + "cms-tls.p12"; //create temporary keystore
        SimpleKeystore tlsKeystore =  new SimpleKeystore(new FileResource(new File(tmpKeystorePath)), "changeit");
        X509Certificate cmsTlsCert = downloadCmsTlsCert(tlsKeystore, new URL(cmsBaseUrl));
        if(cmsTlsCert == null) {
            log.error("All server certificates were rejected; check CMS_TLS_CERT_SHA384");
            throw new AuthenticationException("All server certificates were rejected; check CMS_TLS_CERT_SHA384");
        }
        Properties cmsProperties = new Properties();
        cmsProperties.setProperty("mtwilson.api.url", cmsBaseUrl);
        cmsProperties.setProperty("mtwilson.api.tls.policy.certificate.keystore.file", tmpKeystorePath);
        cmsProperties.setProperty("mtwilson.api.tls.policy.certificate.keystore.password", "changeit");
        cmsProperties.setProperty("mtwilson.api.tls.policy.certificate.sha384", ConfigurationFactory.getConfiguration().get(CMS_TLS_CERT_SHA384));
        cmsProperties.setProperty("mtwilson.tls.cert.sha384", ConfigurationFactory.getConfiguration().get(CMS_TLS_CERT_SHA384));

        CMSClient cmsClient = new CMSClient(cmsProperties);
        X509Certificate cmsCACert = cmsClient.getCACertificate();
        FileUtils.forceDelete(new File(tmpKeystorePath));
        return cmsCACert;
    }

    private static X509Certificate downloadCmsTlsCert(SimpleKeystore keystore, URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, KeyStoreException {
        String trusted = ConfigurationFactory.getConfiguration().get(CMS_TLS_CERT_SHA384);
        if(trusted == null || trusted.isEmpty()) {
            throw new RuntimeException("CMS_TLS_CERT_SHA384 is not configured");
        }
        X509Certificate[] certificates = TlsUtil.getServerCertificates(url, new AnyProtocolSelector());
        for(X509Certificate certificate : certificates) {
            String fingerprint = Sha384Digest.digestOf(certificate.getEncoded()).toHexString();
            if (trusted.equals(fingerprint)) {
                keystore.addTrustedSslCertificate(certificate, fingerprint);
                keystore.save();
                return certificate;
            }
        }
        return null;
    }
}
