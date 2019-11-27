/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.kms.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.*;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.net.NetUtils;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.jaxrs2.client.AASTokenFetcher;
import com.intel.mtwilson.jaxrs2.client.CMSRootCaDownloader;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;

/**
 * Creates a TLS keypair and cms-signed certificate.
 *
 * @author jbuhacoff
 */
public class JettyTlsKeystore extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JettyTlsKeystore.class);

    // constants
    private static final String TLS_ALIAS = "jetty";
    private final String cmsCaFileName = Folders.configuration() + "/cms-ca-cert.pem";

    // configuration keys
    private static final String JETTY_TLS_CERT_DN = "jetty.tls.cert.dn";
    private static final String JETTY_TLS_CERT_IP = "jetty.tls.cert.ip";
    private static final String JETTY_TLS_CERT_DNS = "jetty.tls.cert.dns";
    private static final String JETTY_TLS_KEY_LENGTH_BITS = "jetty.tls.key.length";
    public static final String JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String JAVAX_NET_SSL_KEYSTORETYPE = "javax.net.ssl.keyStoreType";
    public static final String JAVAX_NET_SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final String ENDPOINT_URL = "endpoint.url";
    public static final String CSR_ALGORITHM = "SHA384WithRSA";

    private Configuration config;
    private String keystoreType;
    private Properties properties = new Properties();
    private File keystoreFile;
    private File propertiesFile;
    private Password keystorePassword;
    private String dn;
    private String[] ip;
    private String[] dns;
    private int keyLength;
    private String cmsBaseUrl;
    private String bearerToken;
    
    @Override
    protected void configure() throws Exception {
        config = getConfiguration();
        keystoreType = config.get(JAVAX_NET_SSL_KEYSTORETYPE, "PKCS12");
        String keystorePath = config.get(JAVAX_NET_SSL_KEYSTORE, null);
        if( keystorePath == null ) {
            keystorePath = Folders.configuration()+File.separator+"keystore.p12";
        }
        keystoreFile = new File(keystorePath);

        // to avoid putting any passwords in the configuration file, we
        // get the password from the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(config)) {
            if( passwordVault.contains(JAVAX_NET_SSL_KEYSTOREPASSWORD)) {
                keystorePassword = passwordVault.get(JAVAX_NET_SSL_KEYSTOREPASSWORD);
            }
        }
        // make sure we have a keystore password, generate if necessary
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0 ) {
            keystorePassword = new Password(RandomUtil.randomBase64String(8).replace("=","_").toCharArray());
            log.info("Generated random keystore password");
        }

        /**
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the KMS when using
         * self-signed certificates
         */
        propertiesFile = new File(Folders.configuration()+File.separator+"https.properties");

        // if we already have a keystore file, then we need to know the existing keystore password
        // otherwise it's ok for password to be missing (new install, or creating new keystore) and
        // we'll generate one in execute()
        if( keystoreFile.exists() ) {
            if( keystorePassword == null || keystorePassword.toCharArray().length == 0 ) { configuration("Keystore password has not been generated"); }
        }

        bearerToken = System.getenv(BEARER_TOKEN);
        if (bearerToken == null || bearerToken.isEmpty()){
            configuration("BEARER_TOKEN cannot be empty");
        }

        cmsBaseUrl = config.get("cms.base.url");
        if (cmsBaseUrl == null || cmsBaseUrl.isEmpty()) {
            configuration("CMS Base Url is not provided");
        }

        // mtwilson-core-launcher sets these system properties: mtwilson.application.id (mtwilson) and mtwilson.application.name (Mt Wilson)
        dn = config.get(JETTY_TLS_CERT_DN, "CN="+System.getProperty("mtwilson.application.name", "ISECL")+" TLS Certificate"); // was:  CN=kms
        // we need to know our own local ip addresses/hostname in order to add them to the ssl cert
        ip = getTrustagentTlsCertIpArray();
        dns = getTrustagentTlsCertDnsArray();
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }
        // NOTE: keystore file itself does not need to be checked, we will create it automatically in execute() if it does not exist
        if( (ip == null ? 0 : ip.length) + (dns == null ? 0 : dns.length) == 0 ) {
            configuration("At least one IP or DNS alternative name must be configured");
        }
        keyLength = Integer.parseInt(config.get(JETTY_TLS_KEY_LENGTH_BITS, "3072"));
    }

    @Override
    protected void validate() throws Exception {
        if( !keystoreFile.exists() ) {
            validation("Keystore file was not created");
            return;
        }
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            validation("Keystore password has not been generated");
            return;
        }
        PrivateKeyStore keystore = new PrivateKeyStore(keystoreType, new FileResource(keystoreFile), keystorePassword);
        if( keystore.contains(TLS_ALIAS) ) {
            log.debug("Found TLS key {}", ((X509Certificate)keystore.getCertificates(TLS_ALIAS)[0]).getSubjectX500Principal().getName());
        }
    }

    @Override
    protected void execute() throws Exception {
        // create the keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(keyLength);
    	TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().insecure().build();
        properties.setProperty("cms.base.url", cmsBaseUrl);
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal(dn), keypair.getPublic());
        final List<ASN1Encodable> subjectAlternativeNames = new ArrayList<ASN1Encodable>();
        if( ip != null ) {
            for(String san : ip) {
                log.debug("Adding Subject Alternative Name (SAN) with IP address: {}", san);
                subjectAlternativeNames.add(new GeneralName(GeneralName.iPAddress, san.trim()));
            }
        }
        if( dns != null ) {
            for(String san : dns) {
                log.debug("Adding Subject Alternative Name (SAN) with Domain Name: {}", san);
                subjectAlternativeNames.add(new GeneralName(GeneralName.dNSName, san.trim()));
            }
        }

        if (subjectAlternativeNames.size() > 0) {
            final GeneralNames subjectAltNames = GeneralNames.getInstance(new DERSequence(subjectAlternativeNames.toArray(new ASN1Encodable[] {})));
            p10Builder.addAttribute(
                    PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                    new Extensions(new Extension[] {
                            new Extension(Extension.subjectAlternativeName,false,subjectAltNames.getEncoded())
                    })
            );
        } else {
            log.error("SAN list cannot be empty");
            throw new IOException("SAN list cannot be empty");
        }

        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(CSR_ALGORITHM);
        ContentSigner signer = csBuilder.build(keypair.getPrivate());
        PKCS10CertificationRequest tlscertrequest  = p10Builder.build(signer);

        StringWriter stringWriter1 = new StringWriter();
        JcaPEMWriter JCApemWriter1 = new JcaPEMWriter(stringWriter1);
        JCApemWriter1.writeObject(tlscertrequest);
        JCApemWriter1.close();
        String csrString = stringWriter1.toString();
        X509Certificate[] certificates = null;
        X509Certificate tlsCertificate = null;
        try {
            /**
             * add the cms ca certificate to truststore. password(changeit) need to be
             * provided to facilitate adding other CAs later to the truststore
             */
            // Creating an empty keystore
            String truststoreType = KeyStore.getDefaultType();
            KeyStore keystore = KeyStore.getInstance(truststoreType);
            keystore.load(null, null);

            String extension = "p12";
            if (truststoreType.equalsIgnoreCase("JKS")) {
                extension = "jks";
            }
            String trustStorePath = Folders.configuration()+File.separator+"truststore."+extension;
            File trustStoreFile = new File(trustStorePath);
            // create truststore if not already present
            trustStoreFile.createNewFile();
            FileOutputStream fout = new FileOutputStream(trustStoreFile, false);
            // set alias as "cmsCA" for the CMS CA certificate. every cert added to
            // truststore needs to be associated with an alias
            X509Certificate cmsCACert = CMSRootCaDownloader.downloadCmsCaCert(cmsBaseUrl);
            IOUtils.write(new Pem("CERTIFICATE", cmsCACert.getEncoded()).toString(), new FileOutputStream(new File(cmsCaFileName)));

            keystore.setCertificateEntry("cmsCA", cmsCACert);
            char[] password = "changeit".toCharArray();
            keystore.store(fout, password);
            fout.close();

            ///Now use this truststore to get TLS Certificate
            ///Retrieve the certificate from CMS.

            properties.setProperty("bearer.token", bearerToken);
            certificates = getCMSSignedCertificate(csrString, trustStorePath, password);
            if( certificates == null ) {
                throw new IOException("Cannot create TLS certificate");
            }
            /**
             * save the TLS certificate  signed by CMS to configuration/tls-cert.pem
             */
            String tlsCertPath = Folders.configuration()+File.separator+"tls-cert.pem";
            File tlsCertFile = new File(tlsCertPath);
            FileWriter fileWriter = new FileWriter(tlsCertFile);
            JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter);
            for(X509Certificate cert : certificates) {
                pemWriter.writeObject(cert);
            }
            pemWriter.close();

            FileOutputStream fout1 = new FileOutputStream(trustStoreFile, false);
            tlsCertificate = certificates[0];
            keystore.setCertificateEntry("tls", tlsCertificate);
            for(int i=1; i < certificates.length; i++) {
                keystore.setCertificateEntry("tls-ca-" + i, certificates[i]);
            }

            keystore.store(fout1, password);
        } catch (IOException ex) {
            log.debug("exception while updating tls.cert/truststore {}", ex.getMessage());
        }

        /**
         * Log the same information to a plain text file so admin can easily
         * copy it as necessary for use in a TLS policy or to verify the
         * server's TLS certificate in the browser.
         *
         * NOTE: this is NOT the encrypted configuration file, it's a plaintext
         * Java Properties file to store the TLS certificate fingerprints so
         * the administrator can verify a TLS connection to the service when using
         * self-signed certificates
         */
        Properties properties = new Properties();
        if( propertiesFile.exists() ) {
            properties.load(new StringReader(FileUtils.readFileToString(propertiesFile, Charset.forName("UTF-8"))));
        }
        properties.setProperty("tls.cert.md5", Md5Digest.digestOf(tlsCertificate.getEncoded()).toString());
        properties.setProperty("tls.cert.sha1", Sha1Digest.digestOf(tlsCertificate.getEncoded()).toString());
        properties.setProperty("tls.cert.sha256", Sha256Digest.digestOf(tlsCertificate.getEncoded()).toString());
        properties.setProperty("tls.cert.sha384", Sha384Digest.digestOf(tlsCertificate.getEncoded()).toString());
        StringWriter writer = new StringWriter();
        properties.store(writer, String.format("updated on %s", Iso8601Date.format(new Date())));
        FileUtils.write(propertiesFile, writer.toString(), Charset.forName("UTF-8"));
        log.debug("Wrote https.properties: {}", writer.toString().replaceAll("[\\r\\n]", "|"));

        // look for an existing tls keypair and delete it
        try(PrivateKeyStore keystore = new PrivateKeyStore(keystoreType, new FileResource(keystoreFile), keystorePassword)) {
            String alias = TLS_ALIAS;
            List<String> aliases = keystore.aliases();
            if( aliases.contains(alias) ) {
                keystore.remove(alias);
            }
            // store it in the keystore
            keystore.set(TLS_ALIAS, keypair.getPrivate(), certificates);
        }
        catch(KeyStoreException e) {
            log.debug("Cannot remove existing tls keypair", e);
        }

        // save the settings in configuration
        config.set(KEYSTORE_PASSWORD, new String(keystorePassword.toCharArray()));
        config.set(JETTY_TLS_CERT_DN, dn);
        if( ip != null ) {
            config.set(JETTY_TLS_CERT_IP, StringUtils.join(ip, ","));
        }
        if( dns != null ) {
            config.set(JETTY_TLS_CERT_DNS, StringUtils.join(dns, ","));
        }

        // save the password to the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(config)) {
            passwordVault.set(JAVAX_NET_SSL_KEYSTOREPASSWORD, keystorePassword);
        }

        // save a special endpoint url parameter where clients can access the web server
        config.set(ENDPOINT_URL, getEndpoint());
    }


    private String getEndpoint() {
        // do we have a DNS name configured?
        String endpointHost = null;
        if( dns != null ) {
            for(String hostname : dns) {
                // using "contains" because it can be "localhost" or "ip6-localhost" on some systems
                if( !hostname.contains("localhost") ) {
                    endpointHost = hostname;
                }
            }
        }
        // if no DNS name, do we have an external IP address configured?
        if( endpointHost == null && ip != null ) {
            for(String hostname : ip) {
                // IPv4 127.0.0.1 and IPv6 0:0:0:0:0:0:0:1 and ::1
                if( !hostname.equals("127.0.0.1") && !hostname.equals("0:0:0:0:0:0:0:1") && !hostname.equals("::1") ) {
                    endpointHost = hostname;
                }
            }
        }
        // if no DNS or external IP, just use "localhost" as default
        if( endpointHost == null ) {
            endpointHost = "localhost";
        }
        // do we have a custom port or default port?
        String port = config.get("jetty.port", "80");
        if( port.equals("80") ) {
            return String.format("http://%s", endpointHost); //  http://localhost
        }
        else if( port.equals("443") ) {
            return String.format("https://%s", endpointHost);
        }
        else {
            return String.format("http://%s:%s", endpointHost, port);  //  http://localhost:80
        }
    }

    // note: duplicated from TrustagentConfiguration
    public String getTrustagentTlsCertIp() {
        return config.get(JETTY_TLS_CERT_IP, "");
    }
    // note: duplicated from TrustagentConfiguration
    private String[] getTrustagentTlsCertIpArray() throws SocketException {
        String[] TlsCertIPs = config.get(JETTY_TLS_CERT_IP, "").split(",");
        if (TlsCertIPs != null && !TlsCertIPs[0].isEmpty()) {
            log.debug("Retrieved IPs from configuration: {}", (Object[])TlsCertIPs);
            return TlsCertIPs;
        }
        List<String> TlsCertIPsList = NetUtils.getNetworkAddressList(); // never returns null but may be empty
        String[] ipListArray = new String[TlsCertIPsList.size()];
        if (ipListArray.length > 0) {
            log.debug("Retrieved IPs from network configuration: {}", (Object[])ipListArray);
            return TlsCertIPsList.toArray(ipListArray);
        }
        log.debug("Returning default IP address [127.0.0.1]");
        return new String[]{"127.0.0.1"};
    }
    // note: duplicated from TrustagentConfiguration
    public String getTrustagentTlsCertDns() {
        return config.get(JETTY_TLS_CERT_DNS, "");
    }
    // note: duplicated from TrustagentConfiguration
    private String[] getTrustagentTlsCertDnsArray() throws SocketException {
        String[] TlsCertDNs = config.get(JETTY_TLS_CERT_DNS, "").split(",");
        if (TlsCertDNs != null && !TlsCertDNs[0].isEmpty()) {
            log.debug("Retrieved Domain Names from configuration: {}", (Object[])TlsCertDNs);
            return TlsCertDNs;
        }
        List<String> TlsCertDNsList = NetUtils.getNetworkHostnameList(); // never returns null but may be empty
        String[] dnListArray = new String[TlsCertDNsList.size()];
        if (dnListArray.length > 0) {
            log.debug("Retrieved Domain Names from network configuration: {}", (Object[])dnListArray);
            return TlsCertDNsList.toArray(dnListArray);
        }
        log.debug("Returning default Domain Name [localhost]");
        return new String[]{"localhost"};
    }

    private X509Certificate[] getCMSSignedCertificate(String csrString, String keystore, char[] password) throws Exception {
        try {
            String str = new String(password);
            TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(keystore, str).build();
            CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(cmsBaseUrl), tlsPolicy));
            return cmsClient.getCertificate(csrString, "TLS");
        } catch (IOException ex) {
            log.debug("exception while updating kms.cert/truststore {}", ex.getMessage());
            return null;
        }
    }
}
