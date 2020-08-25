/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import java.util.Properties;
import com.intel.mtwilson.jaxrs2.Quote;

/**
 *
 * @author srajen4x
 */
public class SVSClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SVSClient.class);

    public SVSClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }

    /**
     * Retrieves the Quote Verify from SGX Verification Service
     * @return SVS Quote Verify JSON response
     * @contentTypeReturned JSON
     * @methodType POST
     * @sampleRestCall
     * <pre>
     * https://svs-server.com:12000/svs/v1/verifyQuote
     *
     * Headers:
     * Accept: application/json
     *
     * Output:
     * Sample JSON output:
     * 	{"Status":"Success","Message":"SGX ECDSA Quote Verification is Success","ChallengeKeyType":"RSA","ChallengeRsaPublicKey":"-----BEGIN RSA PUBLIC KEY-----AQABpQLdXanPhculEn+Z/20muP8mvvfRjn0j5T65/ZeDa73W9dIAZEDTVpmLldl4nYEQEVs1HtD4JfvCkvqoXTLvttrd9W09DYsttJNsv95/XK43UeT4tVrBN4mNrV+pXjzbEBBq9j1vPZAQKNSudN7Xjvxh9hMHVLOqjBZSx0wMOhhKUWSf5BZZTJVpQ/kEdqUfwQKz37VJpEgJejmC6P81pwPyT1QnbFuTyYrkKoTs2iwmvpyPql+FPGZ5IBVoPmM6Yxm+ex4RKVg6L8YuSE4otgorlPtdLDMpDL+47N1XL+aDbT3n4uOgChEJzloPGXus6coRJjA7Cm0CTv0/ENYYkQ==-----END RSA PUBLIC KEY-----","EnclaveIssuer":"cd171c56941c6ce49690b455f691d9c8a04c2e43e0a4d30f752fa5285c7ee57f","EnclaveIssuerProdID":"00","EnclaveIssuerExtProdID":"00000000000000000000000000000000","EnclaveMeasurement":"b44ea4fa580a41330239e34002dadd88d913442f7a92b8c32c6f849fe13e33bf","ConfigSvn":"00","IsvSvn":"01","ConfigId":"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"}
     * 
     *
     * @sampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     *   SVSClient svsClient = new SVSClient(properties, new TlsConnection(url, tlsPolicy));
     *   X509Certificate svsCACert = svsClient.quoteVerify();
     * </xmp></pre></div>
     */
    public Response quoteVerify(String quote) {
        log.debug("target: {}", getTarget().getUri().toString());

	Quote quoteData = new Quote();
	quoteData.setQuote(quote);

	Response response = getTarget()
                .path("/verifyQuote")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(quoteData, MediaType.APPLICATION_JSON), Response.class);
	return response;
    }
}
