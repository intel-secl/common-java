/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jackson.bouncycastle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bouncycastle.asn1.ASN1Set;

/**
 *
 * @author rksavino
 */
public abstract class AttributeMixIn {

    public AttributeMixIn() { }
    
    @JsonIgnore
    public abstract ASN1Set getAttrValues();
}
