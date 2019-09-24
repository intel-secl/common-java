/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 *
 * @author arijitgh
 */
public class CMSClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMSClient.class);

    public CMSClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }

    /**
     * Retrieves the CA certificate from Certificate Management Service.
     * @return CMS CA certificate
     * @since ISecL 1.6
     * @contentTypeReturned X_PEM_FILE
     * @methodType GET
     * @sampleRestCall
     * <pre>
     * https://cms-server.com:8443/cms/v1/ca-certificates
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
     *   CMSClient cmsClient = new CMSClient(properties, new TlsConnection(url, tlsPolicy));
     *   X509Certificate cmsCACert = cmsClient.getCACertificate();
     * </xmp></pre></div>
     */
    public X509Certificate getCACertificate() {
        log.info("target: {}", getTarget().getUri().toString());
        X509Certificate cmsCertificate = getTarget()
                .path("/ca-certificates")
                .request()
                .accept(CryptoMediaType.APPLICATION_X_PEM_FILE)
                .get(X509Certificate.class);
        return cmsCertificate;
    }

    /**
     * Retrieves TLS/JWT-Signing/Flavor-Signing certificate from Certificate Management Service. Type of
     * retrieved certificate depends on certificate type provided.
     * @param csr - csr value consists of the string format of the x509 certificate signing request that
     *            is to be provided to CMS. CMS validates the CSR and issues the required certificate.
     * @param certificateType - this parameter provides the type of the certificate that is to be issued by
     *             CMS. It can be one of the three types TLS/JWT-Signing/Flavor-Signing. This value
     *             is provided to the certType query parameter of CMS API URL.
     * @return TLS/JWT-Signing/Flavor-Signing certificate
     * @since ISecL 1.6
     * @contentTypeReturned X_PEM_FILE
     * @methodType POST
     * @sampleRestCall
     * <pre>
     * https://cms-server.com:8443/cms/v1/certificates?certType=Flavor-Signing
     *
     * Headers:
     * Accept: application/x-pem-file
     * Content-Type: application/x-pem-file
     * Authorization: Bearer AAS-Bearer-Token
     *
     * Input:
     * X509Certificate certificate signing request in PEM format:
     * -----BEGIN CERTIFICATE REQUEST-----
     * MIIDijCCAfICAQAwRTEdMBsGA1UECxMUVmVyaWZpY2F0aW9uIFNlcnZpY2UxJDAiBgNVBAMTG0FB
     * UyBKV1QgU2lnbmluZyBDZXJ0aWZpY2F0ZTCCAaIwDQYJKoZIhvcNAQEBBQADggGPADCCAYoCggGB
     * AIu5Ugn6S2JL+1wY6WqmhhJQL+WPsghBXUR83RsRTqFLgcA/CFmcU8SWaS5gt7LU2pxLz6CoBT0p
     * 8xfn7jx7udLR/73MaKpUU4OABCWbJpii9lEiSgjKjDWy4Hfq+agNqIBTPAS+Uv6bqUkr0KhiJp4Q
     * TkpusKjG49KtWFPd4PYlWhQ3ZWnoZcb82rbNBvlAOkYkKRyrBAOraJ0engXgxyLTQaBC6NwVE2s3
     * A6v3M2NwAe5R5DzHbqG6099qpJDxv7BRPbOrjSEBE+L9UofO5Oc2ujcucsYbE1Mc1REnM0k1Kys9
     * EU0YYH5qXGTtiBGJUNEJ83rErlbEIGWc9kS5zhfdTHP4ba30ZzXZEYaGb0KxrDjrgGscbuhAxGvd
     * 3mKIqL0evQ3OiCwbv/GZaER3V0S3teaE7X05WtDSydrk6jhaG032f8FusLH0fM9XkfFeW6mMFSfW
     * wE3P+I2UmGDbYlYDJw6ii3zQvcJxEkSooFsD8r9KyqKeNxEYBTFqJHTKSwIDAQABoAAwDQYJKoZI
     * hvcNAQEMBQADggGBADS/yIbpxBs5rfvnz7teSdXeC9bpwu3VIF/GIWMDdjP1l281yOHxRi75jFKL
     * jWAJpasHSrq9P2tAGTfYR016g23qcs32kSLjWRATQqTlKCMXTfZ5gB3PdSeZ9PDhXQyZBPqX8qLx
     * /Q8oi1e+v4ZxeK3t1aAHuc5vRnZGQWe6AT/BV55xfiMxvX8nNDeYPcsRSur75sY20qrG8f5kZq76
     * 87pirZIAZxVo1orX+L+zAKRUCY04aRwFkUIjnTGapCbg1xS/8DqtufPpEptbEi3NycsP5GSaiwNB
     * cXTw1vYs5ID1z5UbTeL7HyrSEPQsEPmOM/Bz8blkkU4BTJ040p7XmHiiJynRxpLliYZG7ebXXsQ0
     * 2bag+/33KY4RQevjKs5K1omP7E7prpZnU1SJ/L8YDXT8f3gSag/vJwpCnEJjchpij542LhA92exN
     * FR9wDNC1rQgDoA2KHoCV3Jnp88AGHFT9Bc6q+20j4ijuAVgWnGvFUb58t09FTy+o3FYvz3tnvQ==
     * -----END CERTIFICATE REQUEST-----
     *
     * Output:
     * X509Certificate certificate in PEM format:
     * -----BEGIN CERTIFICATE-----
     * MIID3jCCAkagAwIBAgIJAMgLTh9O+9wBMA0GCSqGSIb3DQEBDAUAMCkxETAPBgNVBAsTCG10d2ls
     * c29uMRQwEgYDVQQDEwttdHdpbHNvbi1jYTAeFw0xOTA3MDgwMzQ0NTNaFw0yMDA3MDcwMzQ0NTNa
     * MDQxETAPBgNVBAsTCG10d2lsc29uMR8wHQYDVQQDExZtdHdpbHNvbi1mbGF2b3Itc2lnbmVyMIIB
     * ojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEArbONcYW2k4biqWc0P+zLTdJSrAd5FU0o0H9h
     * lcjEVhnFfJRhQIyFMB6ZGoN/C1P8DHAePVzu7m/h/iBcz1iLCGYsbuq897Gp3/RSOAmpnr5XskEW
     * wYtAzeFXt/z9QfSMxnCauRI/KmMmW6oajMxLt6Fclv+Ka0aHLF7c2UXtHL4RG3m5OHTJOJr+ddIE
     * p+DrV0yTDd1wccxmXshbW4GRbCulidQUBMM3bsm4YcHKBu2gd0d9c0v+ERu2W/e0zpkM9ivHJjuQ
     * wIG7io5ns3uRZi/IBCzVK3M1TLMO30hgO5wCcWddl3EPmx9B1d+RER6LPdvxRg1AlZtgq7km9ift
     * dr8f/n2aeMUbkevG6Dp++xUBItePnRDJyzHjT3MD9BLXWIMXOFPsAPzdeVkrnY2LasTgcvTfMYjT
     * EYQMJ0OseGaI/M7jgJPjiNlNBi65StfphyQKH9p9yPm5VqXtRWqnn1GvrKGWC3CLk5TRQcLCF3JF
     * 2+rWtJJkB8Fqbc8gt7ZzAgMBAAEwDQYJKoZIhvcNAQEMBQADggGBACs+fjrQvcvup3jhsHuBbKd2
     * yV6cWtdljnPUwwfPRYtAodhpJGHociiDIi73RjEilP6cmbm2+IYeFmhRmuURy6jjuZpblFOOBgop
     * zYC4brCDoKexzlXmMdvpHYy83zrxRbOL6Vn8RIMnvprVi5fA9JwdzzbcYkGwqhFCQrfZmolAgkem
     * Z1cCa6JGkd5caATi5a3UQlocXmCrbzEvRa4eZWCqrljqpKznxLpOaw1l//zTu4LyTPDYEB5yx2H6
     * MUHAklkeuERYkU3H/urCggcHCVb1eL5WX50ftYT4W7o6XwSwmtdIEVkvAcAwjYOIm7P40d9Qz1As
     * PDBf5UvUoytUZTdADlsPAcpYxr2cmuUHQtCUS5LkzCE536k6Wnt+W8hGhvrm9I+BWXfNydazorwq
     * MnTKtnbQEYA1O4LYvYi0j1SBqYKrN720KQvQPrjYb222ln6woPQY7oxnHIvz1iGLvxbS7Sd9Ecmj
     * ldHkIehss921siMHO3HWfoBLQ0buCQczIQ==
     * -----END CERTIFICATE-----
     * </pre>
     *
     * @sampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     *   CMSClient cmsClient = new CMSClient(properties, new TlsConnection(url, tlsPolicy));
     *   X509Certificate certificate = cmsClient.getCertificate(csr, certificateType);
     * </xmp></pre></div>
     */

    public X509Certificate getCertificate(String csr, String certificateType) {
        log.info("target: {}", getTarget().getUri().toString());
        X509Certificate certificate = getTarget()
                .path("/certificates")
                .queryParam("certType", certificateType)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + super.getConfiguration().get("bearer.token"))
                .accept(CryptoMediaType.APPLICATION_X_PEM_FILE)
                .post(Entity.entity(csr, CryptoMediaType.APPLICATION_X_PEM_FILE), X509Certificate.class);
        return certificate;
    }
}
