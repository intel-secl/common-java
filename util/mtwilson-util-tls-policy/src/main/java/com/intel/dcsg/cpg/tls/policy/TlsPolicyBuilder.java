/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Use this factory to build a specific TlsPolicy based on
 * your requirements and available configuration. For convenience
 * classes covering the most often used settings, use factory methods
 * in TlsPolicyCommon.
 * 
 * If you turn off confidentiality, authentication, and integrity,
 * then you will get the InsecureTlsPolicy and all the connections will be insecure.
 * If you provide any certificate repository or trust delegate they will be ignored.
 * 
 * If you require confidentiality, authentication, and integrity (the default) but do not provide any
 * certificate repositories, it will not be possible to make any connections that satisfy those requirements
 * without known trusted certificates so all the connections will fail.
 * 
 * If you require confidentiality, authentication, and integrity (the default) and you provide
 * certificate repositories, but do not provide a confirmation delegate, then connections will succeed if
 * the server's certificate or chain can be validated using the specified repositories. If the server
 * cannot be verified the connection will fail.
 * 
 * If you require confidentiality, authentication, and integrity (the default) and you provide
 * certificate repositories, and you provide a confirmation delegate, then connections will succeed if
 * the server's certificate or chain can be validated using the specified repositories. If the server
 * cannot be verified the confirmation delegate will be invoked in order to ask if the server certificate
 * or root CA should be accepted. The delegate is then responsible for prompting the user or invoking
 * any other second channel in order to verify certificate fingerprints. If the delegate answers that
 * the certificate should be accepted the new certificate will be written to the provided mutable 
 * certificate repository and the connection will succeed. If the delegate declines, then the connection
 * will fail.
 * 
 * TODO Additional posisbilities that are currently not implemented:
 * Turn off confidentiality but require authentication and integrity - the TlsPolicy will attempt to use
 * https when possible but will also accept non-SSL http because encryption is not required. 
 * The policy will require a certificate repository & client certificate in order to sign all outgoing requests
 * and verify integrity of incoming requests.  It may be possible to specify the algorithm (HMAC-SHA256, RSA-SHA256, etc)
 * that is used to sign the messages - in the case an HMAC is used the repository will need to contain symmetric keys.
 * 
 * Most common usages are expected to be:
 * 
 * TlsPolicyBuilder.factory().insecure() -  developers temporarily turn off security checks because they can't be bothered to setup the
 *               certificate repositories
 * TlsPolicyBuilder.factory().strict(repository) - requires known certificates and fails w/o prompts if the server's certificate is unknown
 * TlsPolicyBuilder.factory().browser(repository, delegate) - typical browser behavior, uses a known certificate repository but
 *               prompts the user to accept new certificates if they are unknown
 * 
 * See also: 
 * http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#TrustManager
 * http://docs.oracle.com/javase/1.5.0/docs/guide/security/certpath/CertPathProgGuide.html
 * http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/X509TrustManager.html
 * 
 * @author jbuhacoff
 */
public class TlsPolicyBuilder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyBuilder.class);

    private boolean providesConfidentiality = true;
    private boolean providesAuthentication = true;
    private boolean hostnameVerification = true; // only considered when providesAuthentication is enabled
    private boolean providesIntegrity = true;
    private CertificateRepository certificateRepository = null;
    private PublicKeyRepository publicKeyRepository = null;
    private TrustDelegate trustDelegate = null;
    
    public TlsPolicyBuilder() { }
    
    public static TlsPolicyBuilder factory() { return new TlsPolicyBuilder(); }
    
    /**
     * Do not require confidentiality, authentication, or integrity.
     * Such connections are vulnerable to man-in-the-middle attacks.
     * @return 
     */
    public TlsPolicyBuilder insecure() {
        providesConfidentiality = false;
        providesAuthentication = false;
        hostnameVerification = false; // implied when providesAuthentication is false
        providesIntegrity = false;  
        certificateRepository = null;
        publicKeyRepository = null;
        trustDelegate = null;
        return this;
    }
    
    /**
     * Require confidentiality, authentication, and integrity. Provide
     * a repository of trusted certificates and do not allow any additions
     * to it - if a server certificate is not trusted it is rejected.
     * To allow user to accept untrusted certificates, you can use:
     * strict(repository).trustDelegate(delegate).
     * To allow certificates that don't have the hostname in the common name or
     * subject alternative name, you can user:
     * strict(repository).skipHostnameVerification()
     * And to allow the user to accept such untrusted certificates you can combine these:
     * strict(repository).skipHostnameVerification().trustDelegate(delegate)
     * 
     * TODO: instead of "strict" it should be "certificate" with the default
     * strict settings and other things work the same,  and also "publicKey"
     * which would default to the same strict settings but no hostname verification
     * and also can accept a list of public keys instead of certificates.
     * 
     * @param repository
     * @return 
     */
    public TlsPolicyBuilder strict(CertificateRepository repository) {
        providesConfidentiality = true;
        providesAuthentication = true;
        hostnameVerification = true;
        providesIntegrity = true;  
        certificateRepository = repository;
        publicKeyRepository = null;
        trustDelegate = null;
        return this;
    }
    
    /**
     * 
     * @param keystorePath to a java KeyStore (.jks) file
     * @param password
     * @return 
     */
    public TlsPolicyBuilder strictWithKeystore(String keystorePath, String password) throws IOException {
        return strictWithKeystore(new FileResource(new File(keystorePath)), password);
    }
    /**
     * 
     * @param keystoreFile containing a java KeyStore (.jks) file
     * @param password
     * @return 
     */
    public TlsPolicyBuilder strictWithKeystore(File keystoreFile, String password) throws IOException {
        return strictWithKeystore(new FileResource(keystoreFile), password);
    }
    /**
     * 
     * @param keystoreResource containing a java KeyStore (.jks) file
     * @param password
     * @return 
     */
    public TlsPolicyBuilder strictWithKeystore(Resource keystoreResource, String password) throws IOException {
        try {
            CertificateRepository repository = new KeystoreCertificateRepository(keystoreResource, password);
            return strict(repository);
        }
        catch(KeyStoreException e) {
            throw new IOException("Cannot open keystore: "+e.toString(), e);
        }
        catch(NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalArgumentException("Cannot open keystore: "+e.toString(), e);
        }
    }
    

    public TlsPolicyBuilder strict(PublicKeyRepository repository) {
        providesConfidentiality = true;
        providesAuthentication = true;
        hostnameVerification = true;
        providesIntegrity = true;  
        certificateRepository = null;
        publicKeyRepository = repository;
        trustDelegate = null;
        return this;
    }
    
    
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param repository
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public TlsPolicyBuilder insecureTrustFirstCertificate(MutableCertificateRepository repository) throws IOException, CertificateException {
        TrustDelegate delegate = new FirstCertificateTrustDelegate(repository);
        return strict(repository).trustDelegate(delegate).skipHostnameVerification();
    }
    
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param keystorePath relative or absolute path to a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public TlsPolicyBuilder insecureTrustFirstCertificateWithKeystore(File keystoreFile, String password) throws IOException, CertificateException {
        return insecureTrustFirstCertificateWithKeystore(new FileResource(keystoreFile), password);
    }
    /**
     * Similar to the strict policy with two exceptions:
     * 1. hostname verification is disabled; this can be insecure because the host's key could be stolen and used in another host
     * 2. if the certificate repository is empty, the first certificate encountered is automatically trusted and added to the repository
     * @param keystoreResource containing a java KeyStore (.jks) file
     * @param password
     * @return
     * @throws IOException
     * @throws CertificateException 
     */
    public TlsPolicyBuilder insecureTrustFirstCertificateWithKeystore(Resource keystoreResource, String password) throws IOException, CertificateException {
        try {
            MutableCertificateRepository repository = new KeystoreCertificateRepository(keystoreResource, password);
            return insecureTrustFirstCertificate(repository);
        }
        catch(KeyStoreException e) {
            throw new IOException("Cannot open keystore: "+e.toString(), e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot open keystore: "+e.toString(), e);
        }
    }
    
    
    /**
     * Require a policy that provides confidentiality. This is the default.
     * @return 
     */
    public TlsPolicyBuilder providesConfidentiality() {
        providesConfidentiality = true;
        return this;
    }

    /**
     * Do not require a policy that provides confidentiality.
     * @return 
     */
    public TlsPolicyBuilder noConfidentiality() {
        providesConfidentiality = false;
        return this;
    }

    /**
     * Require a policy that provides authentication. This is the default.
     * 
     * Specify a read-only repository of certificates. If your trusted certificates are in
     * multiple locations (for example a global trusted root CA list and a per-host
     * known certificate list) you can provide an aggregate repository instance that combines them.
     * repository.
     * 
     * If you want to allow the user to accept untrusted certificates "just this once" or "always", then
     * you need to also set a trust delegate to handle it.  See the trustDelegate method and the browser
     * method.
     * 
     * @param repository must not be null; you cannot authenticate a server without a list of trusted identities or CA's
     * @return 
     */
    public TlsPolicyBuilder providesAuthentication(CertificateRepository repository) {
        providesAuthentication = true;
        hostnameVerification = true;
        certificateRepository = repository;
        publicKeyRepository = null;
        return this;
    }
    public TlsPolicyBuilder providesAuthentication(PublicKeyRepository repository) {
        providesAuthentication = true;
        hostnameVerification = true;
        certificateRepository = null;
        publicKeyRepository = repository;
        return this;
    }

    /**
     * This should ONLY be used if you maintain a separate repository for each remote host. Otherwise a trusted
     * certificate and private key from one host can be stolen and used on another host.
     * How to use it:
     * 
     * builder.providesAuthentication(repository).skipHostnameVerification()
     * 
     * How revert to enabling hostname verification:
     * 
     * builder.providesAuthentication(repository)   automatically enables hostname verification
     * 
     * @param repository
     * @return 
     */
    public TlsPolicyBuilder skipHostnameVerification() {
        hostnameVerification = false;
        return this;
    }
    
    /**
     * Do not require a policy that provides authentication.
     * 
     * XXX TODO this may be confusing... are we enforcing SERVER authentication or CLIENT authentication to the server?
     * Naturally it seems we are enforcing that the SERVER authenticate to the CLIENT,  because the user of this class
     * is the client so it doesn't make sense for the client to enforce that it authenticates itself to the server... 
     * the server will demand it if it's required. So to support client authentication you only have to call the method
     * that allows you to provide a client keystore with an RSA key or HMAC , whatever the server needs. 
     * @return 
     */
    public TlsPolicyBuilder noAuthentication() {
        providesAuthentication = false;
        hostnameVerification = false;
        return this;
    }

    /**
     * Require a policy that provides message integrity. This is the default.
     * @return 
     */
    public TlsPolicyBuilder providesIntegrity() {
        providesIntegrity = true;
        return this;
    }

    /**
     * Do not require a policy that provides message integrity.
     * @return 
     */
    public TlsPolicyBuilder noIntegrity() {
        providesIntegrity = false;
        return this;
    }
    
    public TlsPolicyBuilder javaTrustStore() {
        try {
            certificateRepository = new KeystoreCertificateRepository(System.getProperty("javax.net.ssl.trustStore"), System.getProperty("javax.net.ssl.trustStorePassword"));
        }
        catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalArgumentException("Cannot load javax.net.ssl.trustStore from "+System.getProperty("javax.net.ssl.trustStore")+": "+e.toString());
        }
        return this;
    }
        
    /**
     * Specify a delegate (callback) implementation to ask if a new unknown certificate should
     * be accepted. THe delegate should prompt the user or use some other second channel to 
     * confirm the certificate fingerprint before accepting it.
     * The delegate may have access to a writable repository of certificates. This means if a server certificate or CA
     * is not present in the repository, and if the trust delegate has determined it should accept the certificate
     * (for example by asking the user) then it can choose to save it into any available mutable certificate repository,
     * or even the one that was passed into the trustedCertificates method.
     * @param repository may be null to clear a previously set value
     * @return 
     */
    public TlsPolicyBuilder trustDelegate(TrustDelegate delegate) {
        trustDelegate = delegate;
        return this;
    }
    

    
// TODO need to combine "known cert" with "trust root and verify hostname" somehow... problem is the hostname verifir and cert checker are used independently !!!    
    public TlsPolicy build() {
        if( !providesConfidentiality && !providesAuthentication && !providesIntegrity ) {
            return new InsecureTlsPolicy();
        }
        if( providesAuthentication && certificateRepository == null ) {
            certificateRepository = ArrayCertificateRepository.EMPTY;
        }
        if( providesConfidentiality && providesAuthentication && providesIntegrity ) {
            if( trustDelegate == null ) {
                if( hostnameVerification ) {
                    return new CertificateTlsPolicy(certificateRepository); // strict mode - trust only certificates in repository and reject all others; do not prompt to accept new certificates.... if certificate repository was set to null, then all connections will be rejected!
                }
                else {
                    if( certificateRepository != null ) {
                        return new PublicKeyTlsPolicy(new PublicKeyCertificateRepository(certificateRepository)); // the PublicKeyCertificateRepository wraps a CertificateRepository and presents a PublicKeyRepository interface to the PublicKeyTlsPolicy
                    }
                    if( publicKeyRepository != null ) {
                        return new PublicKeyTlsPolicy(publicKeyRepository);
                    }
                    throw new UnsupportedOperationException("No support for secure policy without trusted public keys or certificates");
                }
            }
            else {
                if( hostnameVerification ) {
                    return new CertificateTlsPolicy(certificateRepository, trustDelegate); // browser mode with initial set of trusted certificates                
                }
                else {
                    // browser mode with initial set of trusted certificates
                    if( certificateRepository != null ) {
                        return new PublicKeyTlsPolicy(new PublicKeyCertificateRepository(certificateRepository), trustDelegate); 
                    }
                    if( publicKeyRepository != null ) {
                        return new PublicKeyTlsPolicy(publicKeyRepository, trustDelegate);
                    }
                    throw new UnsupportedOperationException("No support for secure policy without trusted public keys or certificates");
                }
            }
        }
        if( !providesConfidentiality && providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for authentication and integrity without encryption"); // TODO see below on implementing signing of messages, possibly with an Authentication (in contrast to Authorziation) header but using http instead of https,   either with RSA or HMAC.  
        }
        if( providesConfidentiality && !providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for encryption and integrity without authentication"); // TODO is there a way to provide an encrypted channel without server authentication? it would be vulnerable to MITM attacks unless the encryption keys are shared in advance. so probably that has to be a requirement - cannot use SSL protocl or DH handshake, must use pre-shared encryption keys.
        }
        if( providesConfidentiality && providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for encryption and authentication without integrity"); // TODO if the channel is encrypted and authenticated, shouldn't it also have integrity?? is there any example of a channel that is encrypted & authenticated but does not do any message integrity checks?  
        }
        if( !providesConfidentiality && !providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for integrity without authentication or encryption"); // only because we don't have a standard protocol right now to handle it... it's basically a checksum using HMAC (so that an attacker can't change the message AND the checksum to match) ... but without authentication it means the server may share an hmac key with multiple clients, so the server doesn't necessarily know WHO is sending it.  
        }
        if( !providesConfidentiality && providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for authentication without integrity or encryption"); // only because we don't have a standard protocol right now to handle it
        }
        if( providesConfidentiality && !providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for confidentiality without authentication or integrity"); // only because we don't have a standard protocol right now to handle it
        }
        /*
        * TODO Additional posisbilities that are currently not implemented:
        * Turn off confidentiality but require authentication and integrity - the TlsPolicy will attempt to use
        * https when possible but will also accept non-SSL http because encryption is not required. 
        * The policy will require a certificate repository & client certificate in order to sign all outgoing requests
        * and verify integrity of incoming requests.  It may be possible to specify the algorithm (HMAC-SHA256, RSA-SHA256, etc)
        * that is used to sign the messages - in the case an HMAC is used the repository will need to contain symmetric keys.
         */
        // if we haven't identified a policy by now, the caller did something wrong
        throw new UnsupportedOperationException("The specified requirements are not supported");
    }
}
