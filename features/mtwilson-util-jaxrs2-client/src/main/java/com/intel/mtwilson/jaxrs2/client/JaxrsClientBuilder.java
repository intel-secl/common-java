/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.net.URL;
import org.glassfish.jersey.client.ClientConfig;
import com.intel.mtwilson.security.http.jaxrs.X509AuthorizationFilter;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature; //jersey 2.10.1
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.File;
import java.util.Properties;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PrefixConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.keplerlake.authz.hmac.client.JaxrsHmacAuthorizationFilter;
import com.intel.mtwilson.jaxrs2.client.retry.RetryProxy;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import com.intel.mtwilson.MyConfiguration;
import com.intel.mtwilson.jaxrs2.feature.JacksonFeature;
import com.intel.mtwilson.retry.Backoff;
import com.intel.mtwilson.retry.ConstantBackoff;
import com.intel.mtwilson.retry.ExponentialBackoff;
import com.intel.mtwilson.retry.RandomBackoff;
import com.intel.mtwilson.security.http.jaxrs.JwtAuthorizationFilter;
import com.intel.mtwilson.security.http.jaxrs.TokenAuthorizationFilter;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.client.ClientRequestFilter;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider; // jersey 2.4.1
import org.glassfish.jersey.logging.LoggingFeature;

/**
 * Examples:
 *
 * <pre>
 * JaxrsClient client = JaxrsClientBuilder.factory().url(url).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().configuration(configuration).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().tlsConnection(tlsConnection).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().url(url).tlsPolicy(tlsPolicy).build();
 * </pre>
 *
 *
 * @author jbuhacoff
 */
public class JaxrsClientBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxrsClientBuilder.class);
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10;
    private static final int DEFAULT_READ_TIMEOUT = 10;
    private static final String DEFAULT_LOGGER_CLASS_PATH = "org.glassfish.jersey.logging.LoggingFeature";

    public static JaxrsClientBuilder factory() {
        return new JaxrsClientBuilder();
    }
    final private ClientConfig clientConfig;
    private Configuration configuration;
    private PasswordKeyStore passwords = null;
    private List<ClientRequestFilter> clientRequestFilters = null;
    private TlsPolicy tlsPolicy;
    private URL url;
    private TlsConnection tlsConnection;
    private HashSet<Class> classRegistrations = null;
    private String proxyHost;
    private Integer proxyPort;
    private Backoff retryBackoff = null;
    private Integer retryMax = null;
    
    public JaxrsClientBuilder() {
        clientConfig = new ClientConfig();
        clientConfig.register(JacksonFeature.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.DateParamConverterProvider.class);
    }

    /**
     * Configures a client using authentication settings in the properties
     * argument. The API URL must be set as mtwilson.api.url or
     * mtwilson.api.baseurl in the properties.
     *
     * To use BASIC password authentication, set mtwilson.api.username and
     * mtwilson.api.password.
     *
     * To use HMAC (MtWilson-specific) authentication, set mtwilson.api.clientId
     * and mtwilson.api.secretKey
     *
     * To use X509 (MtWilson-specific) authentication, set:
     * mtwilson.api.keystore = path to client-keystore.p12
     * mtwilson.api.keystore.password = password protecting client-keystore.p12
     * mtwilson.api.key.alias = alias of private key in the keystore; usually
     * same as username or name of keystore like "client-keystore"
     * mtwilson.api.key.password = password protecting the key, usually same as
     * the keystore password
     *
     * @param properties
     * @return
     */
    public JaxrsClientBuilder configuration(Properties properties) {
        if( properties == null ) {
            throw new NullPointerException("Missing configuration properties");
        }
        log.debug("Building client with properties: {}", properties.stringPropertyNames());
        configuration = new PropertiesConfiguration(properties);
        return this;
    }

    public JaxrsClientBuilder configuration(Configuration configuration) {
        if( configuration == null ) {
            throw new NullPointerException("Missing configuration");
        }
        this.configuration = configuration;
        return this;
    }

    /**
     * New feature for increased password security: You can now pass in a
     * com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore instance from
     * which the password would be retrieved. The alias for the password in the
     * keystore should be the same as the property name in the properties
     * configuration would have been. For EXAMPLE:
     *
     * If mtwilson.properties had mtwilson.api.password=my_password and a
     * PasswordKeyStore instance is given, the JaxrsClientBuilder will first
     * look for passwordKeyStore.get("mtwilson.api.password") and use it if
     * available, and only if it's not available it would check the properties
     * for the value of the original property.
     *
     * In this way, we maintain compatibility with older code while allowing
     * updated code to provide passwords as char[] (inside the Password class)
     * instead of as String.
     *
     * @param passwordKeyStore
     * @return
     */
    public JaxrsClientBuilder passwords(PasswordKeyStore passwordKeyStore) {
        this.passwords = passwordKeyStore;
        return this;
    }
    
    private Password getPassword(String... aliases) {
        // first check the password key store
        if( passwords != null ) {
            for (String alias : aliases) {
                try {
                    if (passwords.contains(alias)) {
                        return passwords.get(alias);
                    }
                } catch (KeyStoreException e) {
                    log.error("Keystore failed to retrieve password: {}", alias, e);
                }
            }
        }
        // second check the configuration (compatibility with existing code)
        if( configuration != null ) {
            for (String alias : aliases) {
                String property = configuration.get(alias);
                if( property != null ) {
                    return new Password(property);
                }
            }
        }
        // password was not found in keystore or configuration
        return null;
    }

    private void authentication() throws KeyManagementException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, CryptographyException {
        if (configuration == null) {
            return;
        }
        log.debug("Configuring client authentication");
        // X509 authorization 
        String keystorePath = configuration.get("login.x509.keystore.file", configuration.get("mtwilson.api.keystore"));
        String keystoreType = configuration.get("login.x509.keystore.type", configuration.get("mtwilson.api.keystore.type")); // MTWKS or JKS or PKCS12
        Password keystorePassword = getPassword("login.x509.keystore.password", "mtwilson.api.keystore.password");
        /*if (keystorePath != null && keystorePassword != null) {
            log.debug("Loading keystore from path {}", keystorePath);
            FileResource resource = new FileResource(new File(keystorePath));
            keystore = new SimpleKeystore(resource, keystorePassword);
        }*/
        String keyAlias = configuration.get("login.x509.key.alias", configuration.get("mtwilson.api.key.alias"));
        Password keyPassword = getPassword("login.x509.key.password", "mtwilson.api.key.password");
        /*if (keystore != null && keyAlias != null && keyPassword != null) {
            log.debug("Registering X509 credentials for {}", keyAlias);
            log.debug("Loading key {} from keystore {}", keyAlias, keystorePath);
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(keyAlias, keyPassword);
            log.debug(credential.getPublicKey().toString());
            clientConfig.register(new X509AuthorizationFilter(credential));
        }*/
        if( keystorePath != null && keyAlias != null ) {
            try {
                log.debug("Loading keystore from path {}", keystorePath);
                PrivateKeyStore keystore = new PrivateKeyStore(keystoreType, new FileResource(new File(keystorePath)), keystorePassword);
                log.debug("Registering X509 credentials for {}", keyAlias);
                if( keyPassword != null ) {
                    log.debug("Loading key {} from keystore {} with key password", keyAlias, keystorePath);
                    PrivateKey privateKey = keystore.getPrivateKey(keyAlias, keyPassword);
                    Certificate[] certificates = keystore.getCertificates(keyAlias);
                    RsaCredentialX509 credential = new RsaCredentialX509(privateKey, (X509Certificate)certificates[0]);
                    log.debug(credential.getPublicKey().toString());
                    clientConfig.register(new X509AuthorizationFilter(credential));
                }
                else {
                    log.debug("Loading key {} from keystore {} with keystore password", keyAlias, keystorePath);
                    PrivateKey privateKey = keystore.getPrivateKey(keyAlias);
                    Certificate[] certificates = keystore.getCertificates(keyAlias);
                    RsaCredentialX509 credential = new RsaCredentialX509(privateKey, (X509Certificate)certificates[0]);
                    log.debug(credential.getPublicKey().toString());
                    clientConfig.register(new X509AuthorizationFilter(credential));
                }
            }
            catch(IOException e) {
                throw new KeyStoreException("Cannot open keystore", e);
            }
        }
        // HMAC authorization (note this is NOT the same as HTTP DIGEST from RFC 2617, that would be login.digest.username and login.digest.password which are not currently implemented)
        String clientId = configuration.get("login.hmac.username", configuration.get("mtwilson.api.clientId"));
        /*Password secretKey = getPassword("login.hmac.password", "mtwilson.api.secretKey");
        if (clientId != null && secretKey != null) {
            log.debug("Registering HMAC credentials for {}", clientId);
            clientConfig.register(new HmacAuthorizationFilter(clientId, secretKey));
        }*/
        Password hmacPassword = getPassword("login.hmac.password", "mtwilson.api.secretKey"); // useful for using hmac authentication based on a password
        String hmacSecretKeyBase64 = configuration.get("login.hmac.key"); // useful for configuring binary keys; must be base64-decoded before use
        String digestAlgorithm = configuration.get("login.hmac.digestAlgorithm", "SHA256");
        if( clientId != null && hmacSecretKeyBase64 != null ) {
            log.debug("Registering HMAC username/key credentials for {}", clientId);
            clientConfig.register(new JaxrsHmacAuthorizationFilter(clientId, Base64.decodeBase64(hmacSecretKeyBase64), digestAlgorithm));
        }
        else if (clientId != null && hmacPassword != null) {
            log.debug("Registering HMAC username/password credentials for {}", clientId);
            clientConfig.register(new JaxrsHmacAuthorizationFilter(clientId, hmacPassword, digestAlgorithm));
        }
        // BASIC authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        String username = configuration.get("login.basic.username", configuration.get("mtwilson.api.username"));
        Password password = getPassword("login.basic.password", "mtwilson.api.password");
        if (username != null && password != null) {
            log.debug("Registering BASIC credentials for {}", username);
            clientConfig.register(HttpAuthenticationFeature.basic(configuration.get("mtwilson.api.username"), configuration.get("mtwilson.api.password"))); // jersey 2.10.1
        }

        // TOKEN authorization is used as part of CSRF protection for the portal
        String loginTokenValue = configuration.get("login.token.value");
        if (loginTokenValue != null) {
            log.debug("Registering TOKEN value {}", loginTokenValue);
            clientConfig.register(new TokenAuthorizationFilter(loginTokenValue));
        }
        String bearerTokenValue = configuration.get("login.bearer.token");
        if (bearerTokenValue != null) {
            log.debug("Registering BEARER value {}", bearerTokenValue);
            clientConfig.register(new TokenAuthorizationFilter("Bearer", bearerTokenValue));
        }

        // JWT authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        Password bearerToken = getPassword("bearer.token");
        if (bearerToken != null) {
            log.debug("Registering JWT value {}", bearerToken);
            clientConfig.register(new JwtAuthorizationFilter(bearerToken));
        }
    }

    /**
     * authorization or other filters may be given directly
     * (instead of parameters to construct them in authenticate method)
     */
    private void filters() {
        if( clientRequestFilters != null ) {
            for(ClientRequestFilter filter : clientRequestFilters) {
                clientConfig.register(filter);
            }
        }

        // JWT authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        Password bearerToken = getPassword("bearer.token");
        if (bearerToken != null) {
            log.debug("Registering JWT value {}", bearerToken);
            clientConfig.register(new JwtAuthorizationFilter(bearerToken));
        }
    }

    private void url() throws MalformedURLException {
        if (url == null) {
            if (configuration != null) {
                url = new URL(configuration.get("endpoint.url", configuration.get("mtwilson.api.url", configuration.get("mtwilson.api.baseurl")))); // example: "http://localhost:8080/v2";
            }
        }
    }

    private void tls() {
        assert url != null;
        if (tlsConnection == null) {
            if (tlsPolicy != null) {
                log.debug("creating TlsConnection from URL and TlsPolicy");
                tlsConnection = new TlsConnection(url, tlsPolicy);
            } else if (configuration != null) {
                PrefixConfiguration tls = new PrefixConfiguration(configuration, "tls.");
                PrefixConfiguration tls2 = new PrefixConfiguration(configuration, "mtwilson.api.tls.");
                if (!tls.keys().isEmpty() || !tls2.keys().isEmpty()) {
                    tlsPolicy = PropertiesTlsPolicyFactory.createTlsPolicy(configuration);
                    log.debug("TlsPolicy is {}", this.tlsPolicy.getClass().getName());
                    tlsConnection = new TlsConnection(url, tlsPolicy);
                    log.debug("set TlsConnection from configuration");
                }
            }
        }
        if (tlsConnection != null) {
            clientConfig.connectorProvider(new HttpUrlConnectorProvider().connectionFactory(new TlsPolicyAwareConnectionFactory(tlsConnection.getTlsPolicy())));
        }
    }

    private void proxy() {
        if (proxyHost == null && configuration != null) {
            proxyHost = configuration.get("proxy.host");
            proxyPort = Integer.valueOf(configuration.get("proxy.port", "8080"));
        }
        if (proxyHost != null) {
            clientConfig.connectorProvider(new HttpUrlConnectorProvider().connectionFactory(new ProxyConnectionFactory(proxyHost, proxyPort)));
        }

    }

    // you can set this instead of url and tlsPolicy
    public JaxrsClientBuilder tlsConnection(TlsConnection tlsConnection) {
        log.debug("set TlsConnection");
        this.tlsConnection = tlsConnection;
        this.url = tlsConnection.getURL();
        this.tlsPolicy = tlsConnection.getTlsPolicy();
        log.debug("TlsPolicy is {}", this.tlsPolicy.getClass().getName());
        return this;
    }

    public JaxrsClientBuilder url(URL url) {
        this.url = url;
        return this;
    }

    public JaxrsClientBuilder tlsPolicy(TlsPolicy tlsPolicy) {
        this.tlsPolicy = tlsPolicy;
        return this;
    }

    public JaxrsClientBuilder register(Class clazz) {
        if (classRegistrations == null) {
            classRegistrations = new HashSet<>();
        }
        classRegistrations.add(clazz);
        return this;
    }

    public JaxrsClientBuilder filter(ClientRequestFilter filter) {
        if( clientRequestFilters == null ) {
            clientRequestFilters = new ArrayList<>();
        }
        clientRequestFilters.add(filter);
        return this;
    }

    public JaxrsClientBuilder filter(List<ClientRequestFilter> filterList) {
        if( clientRequestFilters == null ) {
            clientRequestFilters = new ArrayList<>();
        }
        clientRequestFilters.addAll(filterList);
        return this;
    }

    public JaxrsClientBuilder proxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        return this;
    }

    public JaxrsClientBuilder retry(Backoff backoff) {
        this.retryBackoff = backoff;
        this.retryMax = null;
        return this;
    }
    public JaxrsClientBuilder retry(Backoff backoff, Integer max) {
        this.retryBackoff = backoff;
        this.retryMax = max;
        return this;
    }

    private void retry() {
        if( retryBackoff == null && retryMax == null && configuration != null ) {
            String retryConstantBackoffMillis = configuration.get("retry.backoff.constant");
            String retryExponentialMaxBackoffMillis = configuration.get("retry.backoff.exponential.max");
            String retryRandomMinBackoffMillis = configuration.get("retry.backoff.random.min");
            String retryRandomMaxBackoffMillis = configuration.get("retry.backoff.random.max");
            String retryMaxAttempts = configuration.get("retry.max");
            if( retryConstantBackoffMillis != null ) {
                retryBackoff = new ConstantBackoff(Integer.valueOf(retryConstantBackoffMillis));
            }
            else if( retryExponentialMaxBackoffMillis != null ) {
                retryBackoff = new ExponentialBackoff(Integer.valueOf(retryExponentialMaxBackoffMillis));
            }
            else if( retryRandomMinBackoffMillis != null && retryRandomMaxBackoffMillis != null ) {
                retryBackoff = new RandomBackoff(Integer.valueOf(retryRandomMinBackoffMillis), Integer.valueOf(retryRandomMaxBackoffMillis));
            }
            else if( retryRandomMinBackoffMillis != null || retryRandomMaxBackoffMillis != null ) {
                log.error("Random retry backoff requires both 'min' and 'max' milliseconds");
            }
            if( retryMaxAttempts != null ) {
                retryMax = Integer.valueOf(retryMaxAttempts);
            }
        }
    }

    public JaxrsClient build() {
        MyConfiguration conf = new MyConfiguration(PropertiesConfiguration.toProperties(configuration));
        Integer connectionTimeout = conf.getConfiguration().getInt("mtwilson.config.client.connectTimeout", DEFAULT_CONNECTION_TIMEOUT);
        Integer readTimeout = conf.getConfiguration().getInt("mtwilson.config.client.readTimeout", DEFAULT_READ_TIMEOUT);
        
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout * 1000);
        clientConfig.property(ClientProperties.READ_TIMEOUT, readTimeout * 1000);
        
        try {
            url();
            tls(); // sets tls connection
            proxy(); // optional http proxy -- may override tls settings
            authentication(); // adds to clientConfig
            retry(); // for retry settings in configuration properties
            filters(); // adds to clientConfig

            ClientBuilder builder = ClientBuilder.newBuilder().withConfig(clientConfig);

            if (tlsConnection != null) {
                builder.sslContext(tlsConnection.getSSLContext()); // when commented out,  get pkix path building failure from java's built-in ssl context... when enabled, our custom ssl context doesn't get called at all.
                builder.hostnameVerifier(tlsConnection.getTlsPolicy().getHostnameVerifier());
            }
            if (classRegistrations != null) {
                for (Class clazz : classRegistrations) {
                    builder.register(clazz);
                }
            }
            Client client = builder.build();
            if (org.slf4j.LoggerFactory.getLogger(JaxrsClient.class).isDebugEnabled() ||
                    (configuration != null && Boolean.valueOf(configuration.get(DEFAULT_LOGGER_CLASS_PATH, "true")))
                    ) {
                client.register(new LoggingFeature(Logger.getLogger(DEFAULT_LOGGER_CLASS_PATH),
                        Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, 8192));
            } else {
                client.register(new LoggingFeature());
            }

            if( retryBackoff != null || retryMax != null ) {
                client = (Client)RetryProxy.newClientInstance(client, retryBackoff, retryMax);
            }

            WebTarget target = client.target(url.toExternalForm());

            return new JaxrsClient(client, target, retryBackoff);
        } catch (MalformedURLException | KeyManagementException | FileNotFoundException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
            throw new IllegalArgumentException("Cannot construct client", e);
        }
    }
}
