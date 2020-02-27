/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.mtwilson.crypto.jca.MtWilsonProvider;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class PrivateKeyStoreTest {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrivateKeyStoreTest.class);

        @BeforeClass
        public static void initCryptoProvider() {
            Security.addProvider(new MtWilsonProvider());
        }
        
    private static KeyPair generateRsaKeyPair(int keySizeInBits) throws NoSuchAlgorithmException {
        KeyPairGenerator r = KeyPairGenerator.getInstance("RSA");
        r.initialize(keySizeInBits);
        KeyPair keypair = r.generateKeyPair();
        return keypair;
    }
    
    
    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Security.addProvider(new BouncyCastleProvider());

// build a certificate generator
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal dnName = new X500Principal("cn=example");

// add some options
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(new X509Name("dc=TEST"));
        certGen.setIssuerDN(dnName); // use the same
// yesterday
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
// in 2 years
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
//certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));

// finally, sign the certificate with the private key of the same KeyPair
        X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
        return cert;
    }
        
        @Test
        public void testCreatePrivateKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateEncodingException, IllegalStateException, NoSuchProviderException, SignatureException, InvalidKeyException {
            Password keystorePassword = new Password("changeit");
            Resource resource = new ByteArrayResource();
            KeyPair keypair = generateRsaKeyPair(2048);
            X509Certificate certificate = generateSelfSignedCertificate(keypair);
            try(PrivateKeyStore keystore = new PrivateKeyStore("MTWKS", resource, keystorePassword)) {
                keystore.set("alias1", keypair.getPrivate(), new Certificate[] {certificate});
                keystore.set("alias2", keypair.getPrivate(), new Password("keypasswd"), new Certificate[] {certificate});
            }
            try(PrivateKeyStore keystore = new PrivateKeyStore("MTWKS", resource, keystorePassword)) {
                PrivateKey retrievedPrivateKey = keystore.getPrivateKey("alias1");
                assertEquals(keypair.getPrivate().getAlgorithm(), retrievedPrivateKey.getAlgorithm());
                assertArrayEquals(keypair.getPrivate().getEncoded(), retrievedPrivateKey.getEncoded());
                Certificate[] certificates = keystore.getCertificates("alias1");
                assertNotNull(certificates);
                assertEquals(1, certificates.length);
                X509Certificate retrievedCertificate = (X509Certificate)(certificates[0]);
                assertArrayEquals(certificate.getEncoded(), retrievedCertificate.getEncoded());
            }
            try(PrivateKeyStore keystore = new PrivateKeyStore("MTWKS", resource, keystorePassword)) {
                //PrivateKey retrievedPrivateKey = keystore.getPrivateKey("alias2", new Password("keypasswd"));
                //assertEquals(keypair.getPrivate().getAlgorithm(), retrievedPrivateKey.getAlgorithm());
                //assertArrayEquals(keypair.getPrivate().getEncoded(), retrievedPrivateKey.getEncoded());
                Certificate[] certificates = keystore.getCertificates("alias2");
                assertNotNull(certificates);
                assertEquals(1, certificates.length);
                X509Certificate retrievedCertificate = (X509Certificate)(certificates[0]);
                assertArrayEquals(certificate.getEncoded(), retrievedCertificate.getEncoded());
            }
            
        }
}
