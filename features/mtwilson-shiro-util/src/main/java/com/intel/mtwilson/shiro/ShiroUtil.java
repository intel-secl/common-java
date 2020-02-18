/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.jaxrs2.client.AASClient;

import com.intel.mtwilson.jaxrs2.client.CMSRootCaDownloader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @author jbuhacoff
 */
public class ShiroUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroUtil.class);

    private static final String AAS_API_URL_ENV = "aas.api.url";
    private static final String CMS_BASE_URL_ENV = "cms.base.url";
    private static Map<String, Certificate> kidPubKeyMap = new HashMap<>();
    private static ArrayList<Certificate> caCertList = new ArrayList<>();
    private static ArrayList<Certificate> intermediateCaCertList = new ArrayList<>();
    private static ArrayList<Certificate> jwtSigningCertificateList = new ArrayList<>();
    private final String trustStoreFileName = Folders.configuration() + "/truststore";

    public static boolean subjectUsernameEquals(String username) {
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        Collection<Username> clientUsernameCollection = principalCollection.byType(Username.class);
        Iterator<Username> it = clientUsernameCollection.iterator();
        if (it.hasNext()) {
            Username clientUsername = it.next();
            log.debug("client username: {}", clientUsername.getUsername()); // for example, "admin"  matches portalUser.getUsername() == "admin"
            if (clientUsername.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static String subjectUsername() {
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        Collection<Username> clientUsernameCollection = principalCollection.byType(Username.class);
        Iterator<Username> it = clientUsernameCollection.iterator();
        if (it.hasNext()) {
            Username clientUsername = it.next();
            log.debug("client username: {}", clientUsername.getUsername()); // for example, "admin"
            return clientUsername.getUsername();
        }
        return null;
    }

    public boolean verifyJWTToken(String jwtToken) {
        log.debug("Verifying JWT Token");
        String certHash;

        if (kidPubKeyMap.isEmpty()) {
            if (jwtSigningCertificateList.isEmpty()) {
                try {
                    jwtSigningCertificateList = loadCertificatesFromTrustStore("jwt-signing-cert");
                } catch (Exception exc) {
                    log.error("Error loading JWT signing certificate from trust store: ", exc);
                    return false;
                }
            }
            for (Certificate jwtSigningCert : jwtSigningCertificateList) {
                try {
                    certHash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(
                            jwtSigningCert.getEncoded())).toLowerCase();
                    log.debug("JWT Certificate hash: {}", certHash);
                    kidPubKeyMap.put(certHash, jwtSigningCert);
                } catch (CertificateEncodingException | NoSuchAlgorithmException exc) {
                    log.error("Error getting encoded certificate from trust store: {}", getTrustStorePath());
                    throw new AuthenticationException("Error getting encoded certificate from trust store");
                }
            }
        }
        if (caCertList.isEmpty()) {
            try {
                caCertList = loadCertificatesFromTrustStore("cmsca");
            } catch (Exception exc) {
                log.error("Error loading CA certificate from trust store: ", exc);
                return false;
            }
            if (caCertList.isEmpty()) {
                log.debug("Could not find CA certificate. Downloading from CMS...");
                fetchCMSCACertificate();
            }
        }
        Jws<Claims> claims = null;
        int noOfRetries = 0;
        if (jwtToken != null) {
            while (noOfRetries <= 2) {
                try {
                    Object kid = decodeTokenClaims(jwtToken).getHeader().get("kid");
                    if (kid == null) {
                        throw new CertificateException("JWT token kid not found");
                    }
                    String jwtKid = kid.toString();
                    Certificate jwtSigningCert = kidPubKeyMap.get(jwtKid);
                    if (jwtSigningCert != null) {
                        log.debug("JWT token kid: {}", jwtKid);
                        claims = Jwts.parser().setSigningKey(jwtSigningCert.getPublicKey()).parseClaimsJws(jwtToken);
                        if (intermediateCaCertList.isEmpty()) {
                            intermediateCaCertList = loadCertificatesFromTrustStore("intermediate-ca-cert");
                        }
                        PKIXCertPathBuilderResult certPath =  verifyCertificateChain(jwtSigningCert, caCertList, intermediateCaCertList);
                        break;
                    } else {
                        throw new CertificateException("Certificate not found");
                    }
                } catch (CertificateException exc) {
                    noOfRetries++;
                    if (noOfRetries == 2)
                        throw new AuthenticationException("JWT token/JWT Signing Certificate is invalid: ", exc);
                    log.debug("Certificate present locally is not valid. Downloading certificate from AAS...");
                    fetchJWTSigningCertificate();
                } catch (Exception exc) {
                    throw new AuthenticationException("JWT token provided is invalid: ", exc);
                }
            }
        } else {
            throw new AuthenticationException("No JWT token present in request");
        }
        return true;
    }

    private void fetchJWTSigningCertificate() {
        log.debug("Downloading JWT Signing certificate from AAS..");
        String certHash;
        X509Certificate[] jwtSigningCertificateChain;
        try {
            String aasApiUrl = ConfigurationFactory.getConfiguration().get(AAS_API_URL_ENV);
            AASClient client = new AASClient(new Properties(),
                    new TlsConnection(new URL(aasApiUrl), TlsPolicyBuilder.factory().insecure().build()));

            jwtSigningCertificateChain = client.getJwtSigningCertificate();

            if(jwtSigningCertificateChain != null && jwtSigningCertificateChain.length > 0) {
                X509Certificate jwtSigningCertificate = jwtSigningCertificateChain[0];
                certHash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(
                        jwtSigningCertificate.getEncoded())).toLowerCase();
                kidPubKeyMap.put(certHash, jwtSigningCertificate);
                jwtSigningCertificateList.add(jwtSigningCertificate);
                storeCertificate(jwtSigningCertificate, "jwt-signing-cert-" + getCertificateHash(jwtSigningCertificate));
                for (int i = 1; i < jwtSigningCertificateChain.length; i++ ) {
                    storeCertificate(jwtSigningCertificateChain[i], "intermediate-ca-cert" + getCertificateHash(jwtSigningCertificateChain[i]));
                }
                log.debug("JWT signing certificate downloaded");
            } else {
                throw new AuthenticationException("Could not fetch JWT signing certificates");
            }
        } catch (CertificateEncodingException | NoSuchAlgorithmException exc) {
            log.error("Error getting encoded certificate from AAS: {}", exc.getMessage());
            throw new AuthenticationException("Error getting encoded certificate");
        } catch (IOException exc) {
            log.error("Error writing JWT certificate to file: {}", exc.getMessage());
            throw new AuthenticationException("Error writing JWT certificate to file");
        } catch (Exception e) {
            log.error("Error fetching JWT certificate from AAS");
            throw new AuthenticationException("Error fetching JWT certificate from AAS");
        }
    }

    private void fetchCMSCACertificate() {
        log.debug("Downloading CA certificate from CMS...");
        try {
            X509Certificate cmsCACertificate = CMSRootCaDownloader.downloadCmsCaCert(ConfigurationFactory.getConfiguration().get(CMS_BASE_URL_ENV));
            log.debug("CMS CA certificate downloaded");
            storeCertificate(cmsCACertificate, "cmsca");
        } catch (IOException exc) {
            log.error("Error loading configuration from properties file: {}", exc.getMessage());
            throw new AuthenticationException("Error loading configuration from properties file");
        } catch (Exception e) {
            log.error("Error fetching CA certificate from CMS");
            throw new AuthenticationException("Error fetching CA certificate from CMS");
        }
    }

    public Jwt decodeTokenClaims(String token) {
        int index = token.lastIndexOf('.');
        String tokenWithoutSignature = token.substring(0, index + 1);
        try {
            DefaultJwtParser parser = new DefaultJwtParser();
            return parser.parse(tokenWithoutSignature);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException ex) {
            log.warn("Exception while parsing JWT token for claims");
        }
        return null;
    }

    private String getTrustStorePath() {
        if (KeyStore.getDefaultType().equalsIgnoreCase("JKS")) {
            return trustStoreFileName + ".jks";
        }
        return trustStoreFileName + ".p12";
    }

    private void storeCertificate (X509Certificate certificate, String alias) throws Exception {
        KeyStore keystore = loadTrustStore();
        FileOutputStream keystoreFOS = new FileOutputStream(getTrustStorePath());
        try {
            keystore.setCertificateEntry(alias, certificate);
            keystore.store(keystoreFOS, "changeit".toCharArray());
        } catch (Exception exc) {
            throw new Exception("Error storing certificate in keystore", exc);
        }finally {
            keystoreFOS.close();
        }

    }

    private String getCertificateHash(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        String digestHex = DatatypeConverter.printHexBinary(digest);
        return digestHex.toLowerCase().substring(0, 10);
    }

    private ArrayList<Certificate> loadCertificatesFromTrustStore(String aliasPrefix) throws Exception {
        KeyStore keyStore = loadTrustStore();
        List<String> aliasList = Collections.list(keyStore.aliases());
        ArrayList<Certificate> certificates = new ArrayList<>();

        for (String alias : aliasList) {
            if (alias.contains(aliasPrefix)) {
                certificates.add(keyStore.getCertificate(alias));
            }
        }
        return certificates;
    }

    private KeyStore loadTrustStore() throws Exception{
        FileInputStream keystoreFIS = new FileInputStream(getTrustStorePath());
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keystoreFIS, "changeit".toCharArray());
        } catch (Exception exc) {
            throw new Exception("Error loading trust store", exc);
        } finally {
            keystoreFIS.close();
        }
        return keyStore;
    }

    public static PKIXCertPathBuilderResult verifyCertificateChain(
            Certificate cert, ArrayList<Certificate> trustedRootCerts,
            ArrayList<Certificate> intermediateCerts) throws GeneralSecurityException, InvalidParameterException {

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate((X509Certificate) cert);

        // Create the trust anchors (set of root CA certificates)
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        for (Certificate trustedRootCert : trustedRootCerts) {
            trustAnchors.add(new TrustAnchor((X509Certificate)trustedRootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

        // Disable CRL checks (this is done manually as additional step)
        //TODO can be removed when CRL feature is used
        pkixParams.setRevocationEnabled(false);

        // Specify a list of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        pkixParams.addCertStore(intermediateCertStore);

        // Build and verify the certification chain
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        return (PKIXCertPathBuilderResult) builder.build(pkixParams);
    }

}
