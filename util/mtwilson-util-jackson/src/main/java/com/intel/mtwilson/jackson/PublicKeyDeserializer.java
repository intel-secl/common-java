/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import java.io.IOException;
import java.security.PublicKey;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDeserializer extends JsonDeserializer<PublicKey> {

    @Override
    public PublicKey deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        byte[] bytes = jp.getBinaryValue();
        try {
            PublicKey publicKey = RsaUtil.decodeDerPublicKey(bytes);
            return publicKey;
        }
        catch(Exception e) {
            throw new IOException("Cannot read public key", e);
        }
    }
    
}
