/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.rfc822;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class Base64Decoder implements Decoder {
    @Override
    public byte[] decode(byte[] base64) {
        return Base64.decodeBase64(base64);
    }
}
