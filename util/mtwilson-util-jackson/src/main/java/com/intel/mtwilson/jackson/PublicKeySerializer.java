/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.security.PublicKey;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeySerializer extends JsonSerializer<PublicKey> {

    @Override
    public void serialize(PublicKey t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        jg.writeBinary(t.getEncoded());
    }
    
}
