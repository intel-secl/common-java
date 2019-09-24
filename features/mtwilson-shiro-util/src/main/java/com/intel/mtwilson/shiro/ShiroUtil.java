/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.*;
import java.util.*;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.jaxrs2.client.AASClient;

import javax.xml.bind.DatatypeConverter;

import com.intel.mtwilson.jaxrs2.client.CMSClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * @author jbuhacoff
 */
public class ShiroUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroUtil.class);

    private static final String AAS_API_URL_ENV = "aas.api.url";
    private static final String CMS_BASE_URL_ENV = "cms.base.url";
    private static Map<String, Certificate> kidPubKeyMap = new HashMap<>();
    private final String trustedJwtCertDirName = Folders.configuration() + "/trustedjwt";
    private final String trustedCAFile = Folders.configuration() + "/trustedca/root-ca-cert.pem";

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

    public boolean verifyJWTToken(String jwtToken, String propertiesFile) {
        log.debug("Verifying JWT Token");
        File[] jwtCertFilesList = new File(trustedJwtCertDirName).listFiles();
        Certificate jwtSigningCertX509;
        String certHash;
        if (jwtCertFilesList == null) {
            fetchJWTSigningCertificate(propertiesFile);
            jwtCertFilesList = new File(trustedJwtCertDirName).listFiles();
        }
        if (jwtCertFilesList != null && kidPubKeyMap.isEmpty()) {
            for (final File jwtCertFile : jwtCertFilesList) {
                jwtSigningCertX509 = readCertificateFromFile(jwtCertFile.toString());
                try {
                    if (jwtSigningCertX509 != null)
                        certHash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(
                                jwtSigningCertX509.getEncoded())).toLowerCase();
                    else
                        continue;
                    log.debug("JWT Certificate hash: {}", certHash);
                    kidPubKeyMap.put(certHash, jwtSigningCertX509);
                } catch (CertificateEncodingException | NoSuchAlgorithmException exc) {
                    log.error("Error getting encoded certificate from file: {}", jwtCertFile);
                    throw new AuthenticationException("Error getting encoded certificate");
                }
            }
        }
        Certificate caCertX509 = readCertificateFromFile(trustedCAFile);
        if (caCertX509 == null) {
            log.debug("Could not find CA certificate. Downloading from CMS...");
            caCertX509 = fetchCMSCACertificate(propertiesFile);
            if (caCertX509 == null)
                log.error("Error fetching CA Certificate from CMS");
                return false;
        }
        Jws<Claims> claims = null;
        int noOfRetries = 0;
        if (jwtToken != null) {
            while (noOfRetries <= 2) {
                try {
                    String jwtKid = decodeTokenClaims(jwtToken).getHeader().get("kid").toString();
                    Certificate jwtSigningCert = kidPubKeyMap.get(jwtKid);
                    if (jwtSigningCert != null) {
                        log.debug("JWT token kid: {}", jwtKid);
                        claims = Jwts.parser().setSigningKey(jwtSigningCert.getPublicKey()).parseClaimsJws(jwtToken);
                        jwtSigningCert.verify(caCertX509.getPublicKey());
                        break;
                    } else {
                        throw new CertificateException("Certificate not found");
                    }
                } catch (SignatureException | CertificateException exc) {
                    noOfRetries++;
                    if (noOfRetries == 2)
                        throw new AuthenticationException("JWT token provided is invalid");
                    log.debug("Certificate present locally is not valid. Downloading certificate from AAS...");
                    fetchJWTSigningCertificate(propertiesFile);
                } catch (Exception exc) {
                    throw new AuthenticationException("JWT token provided is invalid");
                }
            }
        } else {
            throw new AuthenticationException("No JWT token present in request");
        }
        log.info("JWT token claims: {}", claims);
        return true;
    }

    private X509Certificate fetchJWTSigningCertificate(String propertiesFile) {
        log.debug("Downloading JWT Signing certificate from AAS..");
        Configuration loadedConfiguration;
        String certHash;
        try {
            loadedConfiguration = loadConfiguration(propertiesFile);
        } catch (IOException exc) {
            log.error("Error loading configuration from properties file");
            throw new AuthenticationException("Error loading configuration from properties file");
        }
        X509Certificate jwtSigningCertificate;
        try {
            String aasApiUrl = loadedConfiguration.get(AAS_API_URL_ENV);
            AASClient client = new AASClient(new Properties(),
                    new TlsConnection(new URL(aasApiUrl), TlsPolicyBuilder.factory().insecure().build()));

            jwtSigningCertificate = client.getJwtSigningCertificate();

            certHash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(
                    jwtSigningCertificate.getEncoded())).toLowerCase();

            kidPubKeyMap.put(certHash, jwtSigningCertificate);

            PemWriter writer = new PemWriter(new FileWriter(trustedJwtCertDirName + "/jwt-" + certHash.substring(0, 10) + ".pem"));
            writer.writeObject(new PemObject("CERTIFICATE", jwtSigningCertificate.getEncoded()));
            writer.close();
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
        return jwtSigningCertificate;
    }

    private X509Certificate fetchCMSCACertificate(String propertiesFile) {
        log.debug("Downloading CA certificate from CMS...");
        try {
            Configuration loadedConfiguration = loadConfiguration(propertiesFile);
            CMSClient cmsClient = new CMSClient(new Properties(),
                    new TlsConnection(new URL(loadedConfiguration.get(CMS_BASE_URL_ENV)), TlsPolicyBuilder.factory().insecure().build()));
            X509Certificate cmsCACertificate = cmsClient.getCACertificate();
            PemWriter writer = new PemWriter(new FileWriter(trustedCAFile));
            writer.writeObject(new PemObject("CERTIFICATE", cmsCACertificate.getEncoded()));
            writer.close();
            return cmsCACertificate;
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
        DefaultJwtParser parser = new DefaultJwtParser();
        return parser.parse(tokenWithoutSignature);
    }

    public Certificate readCertificateFromFile(String jwtCertDir) {
        Certificate certX509 = null;
        try {
            String cert = new String(Files.readAllBytes(Paths.get(jwtCertDir)));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certX509 = cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
        } catch (IOException exc) {
            log.error("Cannot read certificate from file: {}", exc.getMessage());
        } catch (CertificateException exc) {
            log.error("Error decoding certificate: {}", exc.getMessage());
        }
        return certX509;
    }

    private Configuration loadConfiguration(String propertiesFile) throws IOException {
        ConfigurationProvider configurationProvider = ConfigurationFactory.createConfigurationProvider(new File(propertiesFile));
        return configurationProvider.load();
    }
}
