/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.UserCredential;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 *
 * @author rawatar
 */
public class AASClient extends MtWilsonClient{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AASClient.class);

    public AASClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }

    /**
     * Retrieves the JWT token from Authentication Authorization Service.
     * @return AAS JWT token
     * @since ISecL 1.6
     * @contentTypeReturned JWT
     * @methodType POST
     * @sampleRestCall
     * <pre>
     * https://aas-server.com:port/aas/token
     *
     * Headers:
     * Accept: application/jwt
     * Content-Type: application/json
     *
     * Output:
     * JWT token in String format:
     * eyJhbGciOiJSUzM4NCIsImtpZCI6ImVlMDQxMmZmYWM1YWJmNThlYmY2NWUwNjY4OWNiMTk4ZDFmYTE0NzEiLCJ0eXAiOiJKV1QifQ.eyJyb2xlcyI6W3sic2VydmljZSI6IkNNUyIsIm5hbWUiOiJDZXJ0QXBwcm92ZXIiLCJjb250ZXh0IjoiQ049VlMgRmxhdm9yIFNpZ25pbmcgQ2VydGlmaWNhdGU7Y2VydFR5cGU9Rmxhdm9yLVNpZ25pbmcifSx7InNlcnZpY2UiOiJDTVMiLCJuYW1lIjoiQ2VydEFwcHJvdmVyIiwiY29udGV4dCI6IkNOPVdQTSBGbGF2b3IgU2lnbmluZyBDZXJ0aWZpY2F0ZTtjZXJ0VHlwZT1GbGF2b3ItU2lnbmluZyJ9LHsic2VydmljZSI6IkNNUyIsIm5hbWUiOiJDZXJ0QXBwcm92ZXIiLCJjb250ZXh0IjoiQ049S2V5IFNlcnZlciBUTFMgQ2VydGlmaWNhdGU7U0FOPTEyNy4wLjAuMSxsb2NhbGhvc3QsMTAuMS43MC42MztjZXJ0VHlwZT1UTFMifSx7InNlcnZpY2UiOiJDTVMiLCJuYW1lIjoiQ2VydEFwcHJvdmVyIiwiY29udGV4dCI6IkNOPU10IFdpbHNvbiBUTFMgQ2VydGlmaWNhdGU7U0FOPTEyNy4wLjAuMSxsb2NhbGhvc3QsMTAuMS43MC42MztjZXJ0VHlwZT1UTFMifV0sImV4cCI6MTU2OTM5MzEwMCwiaWF0IjoxNTY5MjIwMzAwLCJpc3MiOiJBQVMgSldUIElzc3VlciIsInN1YiI6InN1cGVyYWRtaW4ifQ.oyr8n-i_TGtVAAFfS6jgcm5bHTvX-dx21IhYfH5W1ymLHZkpzM8U8_9YCbldBSEZLCgHrTYBkxnDsa0ZfQuTQ2DRdoJZLrd_ZGOwAWMPkKbf_gyZWZDnBaEtXWDsUFTm26_o0lFHMO2FTLkBJzFJv1ISXrRoDOYIkUvMbHVOiMb4OyDG_yGACDSziiEvmd6DKbsSPUNnYLHiWL2wsueT6hCsk5Zj0CSM5kKfZSzjIcfo5HVMY4Ru5L6bHKvL6EXL3RxFctFtwzTv0ei4_OIPhtefidq9X5b2L2SX5RSgWoM_-IpYYlsGP-EWC2fXgWYbaADDVXznXu4nkx2Ih3te2d5r3s2ataSdIhKvJRsbQAt9pWNw21SUJEiDMf1TCbeNihvevnCWDVon2kfqZJmciNpNyQlbwt4QLbtXH48TBtjEO3fmU1sp1r52KNAMVYqJh6p20y77_Z0yEUzK0rzGGxrTCPnuZN5YN4vOqLIK3YDXTRwKyGTL3UUXpdWRRxfR
     * </pre>
     *
     * @sampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     *   AASClient aasClient = new AASClient(properties, new TlsConnection(url, tlsPolicy));
     *   String jwtToken = aasClient.getToken();
     * </xmp></pre></div>
     */
    public String getToken(UserCredential credential) {
        log.info("target: {}", getTarget().getUri().toString());
        String token = getTarget()
                .path("/token")
                .request()
                .accept(CryptoMediaType.APPLICATION_JWT)
                .post(Entity.entity(credential, MediaType.APPLICATION_JSON), String.class);
        return token;
    }

    /**
     * Retrieves the JWT signing certificate from Authentication Authorization Service.
     * @return AAS JWT signing certificate
     * @since ISecL 1.6
     * @contentTypeReturned X_PEM_FILE
     * @methodType GET
     * @sampleRestCall
     * <pre>
     * https://aas-server.com:port/aas/noauth/jwt-certificates
     *
     * Headers:
     * Accept: application/x-pem-file
     *
     * Output:
     * X509Certificate certificate in PEM format:
     * -----BEGIN CERTIFICATE-----
     * MIIDvjCCAiagAwIBAgIBADANBgkqhkiG9w0BAQwFADAQMQ4wDAYDVQQDEwVDTVND
     * QTAeFw0xOTA3MjMwNzMxMTFaFw0yNDA3MjMwNzMxMTJaMBAxDjAMBgNVBAMTBUNN
     * U0NBMIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAxaL5AP5k3N+Z4dMq
     * cJ0CzE0KtLmRPGXbkqNo8GYNFAbNGgBZFUNryGwEAEtwpxYljXZ/ajYjdBHe8IsO
     * x0WDkp1SbATzSAS4mkOSDotTdy7Ry/o6BvuSTcgaLIg0wCkMSRluXv1oLmx6pbBi
     * 3oP6yGPbMHJd7bbxT7Bx/c6HGluupmL5rnxcf9MkOHP2qicDK8ntLK0yHqE1b6hN
     * dMMQQpEgOD/SNg5qILXJiYM/ymEjSwl4pka7mmWmB3QwB97sPs2NI6lw+Uk1blyJ
     * iQGG9dEmVLJOgWYEaWTEQsQpOag7YSJHDoUQ7XHDYuTJCrEPi6Ns+CqOY9Nl5zYh
     * /brefbmvReuDf+NIM/1gt5zZvPGYqZlAKgXbFEngjEM0NKL83f02HvohE9KeDeZj
     * RuOKDjb3UjrWzk2mULYSTTOajxtyjmjZXAq2QQ4Q01mTQAtiIkIrVVObN6U6Q/Tv
     * OkfGvX93Ipno8WvmzpZyE3pRnWAmmvaIcN92XZCOvaLYpocdAgMBAAGjIzAhMA4G
     * A1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBDAUAA4IB
     * gQC//CUtkt1lIgXOmTSNEBX4Go/Do6sMwWikPKq+RMW+PPxSj3Gl+uqzQJEaTtPq
     * zlvA/EINdm1y3EA0HuQ5eqOFOX0OjhQsbLjAio7vlOq7Ae911J7ePcPGbD2B72Vy
     * tPU1ou0gDiIV6xJECkMBzuQCBdnn4h31xN3hWm9lRzui7+cKM4VPOSihUoNRKaR/
     * L8eu0WlC2H1lPs6v6UtfA33g8uycsA0cBgyKStQgc75q3e5Hujd7oD1OZfPpM4of
     * /ocuYttUaQPdx1KFpdLqz7S+TEaxvQxnzR9G/WB4OZQZbRkEF+j0E+H91eIVySWi
     * S59icGYUsquVms48+jv5Nu1lY5NN/sLGjSqg/bmRbZ60oBYDt3IHT4vxoP2VnsqC
     * xW8rHoPnha+4M5mN04s1/6GeZD3T1Fm9OqGxrmzCmvu1OGRMbU4fxYtIwiNMI2x4
     * IR2mQmv33mynBzKN5tEEw+DU1wzypeIElz2LFvbALHKhhPmCJ4YzW8EGIrVKbTFP
     * M4s=
     * -----END CERTIFICATE-----
     * </pre>
     *
     * @sampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     *   AASClient aasClient = new AASClient(properties, new TlsConnection(url, tlsPolicy));
     *   X509Certificate jwtSigningCert = aasClient.getJwtSigningCertificate();
     * </xmp></pre></div>
     */
    public X509Certificate getJwtSigningCertificate() {
        log.info("target: {}", getTarget().getUri().toString());
        X509Certificate certificate = getTarget()
                .path("/noauth/jwt-certificates")
                .request()
                .accept(CryptoMediaType.APPLICATION_X_PEM_FILE)
                .get(X509Certificate.class);
        return certificate;
    }
}
