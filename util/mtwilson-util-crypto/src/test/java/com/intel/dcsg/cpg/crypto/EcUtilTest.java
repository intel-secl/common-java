/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import static com.intel.dcsg.cpg.crypto.EcUtil.decodePemPrivateKey;
import java.security.PrivateKey;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author skamal
 */
public class EcUtilTest {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testDecodePemPrivateKey() throws CryptographyException {
        String pem = "-----BEGIN PRIVATE KEY-----\n" +
"MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQg8B1zUTflmBWRvLTo4Fw8\n" +
"gKXOyB62dWvhYbtsqDp/ZCChRANCAATlnAklBOGc5Fy0Wr53hPChBqJUZIK7gDFL\n" +
"GCY0AoZwCazBPGFCfHyXFOdsDKPklXHmy6e7AZFUqGi2zgkdOFma\n" +
"-----END PRIVATE KEY-----";

        PrivateKey pk = decodePemPrivateKey(pem);
        log.debug("Private Key: {}", pk.toString());
    }
}
