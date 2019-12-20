/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.security.http.jaxrs;

import com.intel.dcsg.cpg.crypto.key.password.Password;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import java.io.IOException;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 *
 * Sample usage:
 *
 clientConfig = new ClientConfig();
 clientConfig.register(new JwtAuthorizationFilter("token-value"));
 *
 * Example http header added:
 * <pre>
 * Authorization: Bearer eyJhbGciOiJSUzM4NCIsImtpZCI6ImVlMDQxMmZmYWM1YWJmNThlYmY2NWUwNjY4OWNiMTk4ZDFmYTE0NzEiLCJ0eXAiOiJKV1QifQ.eyJyb2xlcyI6W3sic2VydmljZSI6IkNNUyIsIm5hbWUiOiJDZXJ0QXBwcm92ZXIiLCJjb250ZXh0IjoiQ049VlMgRmxhdm9yIFNpZ25pbmcgQ2VydGlmaWNhdGU7Y2VydFR5cGU9Rmxhdm9yLVNpZ25pbmcifSx7InNlcnZpY2UiOiJDTVMiLCJuYW1lIjoiQ2VydEFwcHJvdmVyIiwiY29udGV4dCI6IkNOPVdQTSBGbGF2b3IgU2lnbmluZyBDZXJ0aWZpY2F0ZTtjZXJ0VHlwZT1GbGF2b3ItU2lnbmluZyJ9LHsic2VydmljZSI6IkNNUyIsIm5hbWUiOiJDZXJ0QXBwcm92ZXIiLCJjb250ZXh0IjoiQ049S2V5IFNlcnZlciBUTFMgQ2VydGlmaWNhdGU7U0FOPTEyNy4wLjAuMSxsb2NhbGhvc3QsMTAuMS43MC42MztjZXJ0VHlwZT1UTFMifSx7InNlcnZpY2UiOiJDTVMiLCJuYW1lIjoiQ2VydEFwcHJvdmVyIiwiY29udGV4dCI6IkNOPU10IFdpbHNvbiBUTFMgQ2VydGlmaWNhdGU7U0FOPTEyNy4wLjAuMSxsb2NhbGhvc3QsMTAuMS43MC42MztjZXJ0VHlwZT1UTFMifV0sImV4cCI6MTU2OTM5MzEwMCwiaWF0IjoxNTY5MjIwMzAwLCJpc3MiOiJBQVMgSldUIElzc3VlciIsInN1YiI6InN1cGVyYWRtaW4ifQ.oyr8n-i_TGtVAAFfS6jgcm5bHTvX-dx21IhYfH5W1ymLHZkpzM8U8_9YCbldBSEZLCgHrTYBkxnDsa0ZfQuTQ2DRdoJZLrd_ZGOwAWMPkKbf_gyZWZDnBaEtXWDsUFTm26_o0lFHMO2FTLkBJzFJv1ISXrRoDOYIkUvMbHVOiMb4OyDG_yGACDSziiEvmd6DKbsSPUNnYLHiWL2wsueT6hCsk5Zj0CSM5kKfZSzjIcfo5HVMY4Ru5L6bHKvL6EXL3RxFctFtwzTv0ei4_OIPhtefidq9X5b2L2SX5RSgWoM_-IpYYlsGP-EWC2fXgWYbaADDVXznXu4nkx2Ih3te2d5r3s2ataSdIhKvJRsbQAt9pWNw21SUJEiDMf1TCbeNihvevnCWDVon2kfqZJmciNpNyQlbwt4QLbtXH48TBtjEO3fmU1sp1r52KNAMVYqJh6p20y77_Z0yEUzK0rzGGxrTCPnuZN5YN4vOqLIK3YDXTRwKyGTL3UUXpdWRRxfR
 * </pre>
 *
 * @author rawatar
 * @since 1.6
 */
@Priority(Priorities.AUTHORIZATION)
public class JwtAuthorizationFilter implements ClientRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private final Password tokenValue;

    public JwtAuthorizationFilter(String tokenValue) {
        this.tokenValue = new Password(tokenValue);
    }
    public JwtAuthorizationFilter(Password tokenValue) {
        this.tokenValue = tokenValue;
    }


    /**
     * This method assumes that the entity body of the request is either null or a String or
     * has a toString() method that returns the String that should be signed.
     *
     * @param requestContext
     * @return
     * @throws IOException
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Modify the request
        try {
            String header = String.format("Bearer %s", new String(tokenValue.toCharArray()));
            requestContext.getHeaders().add("Authorization", header);
        }
        catch(Exception e) {
            throw new IOException(e);
        }

    }

}
