/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jackson.bouncycastle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;

import org.bouncycastle.asn1.ASN1Primitive;

/**
 *
 * @author rksavino
 */
public abstract class ASN1EncodableMixIn {

    @JsonIgnore
    public abstract byte[] getEncoded() throws IOException;

    @JsonIgnore
    public abstract byte[] getDEREncoded();

    @JsonIgnore
    public abstract ASN1Primitive getDERObject();
}
