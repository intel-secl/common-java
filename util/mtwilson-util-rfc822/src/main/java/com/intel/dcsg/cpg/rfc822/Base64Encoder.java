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
public class Base64Encoder implements Encoder {
    private boolean chunkOutput = true;
    public void setChunk(boolean chunkOutput) {
        this.chunkOutput = chunkOutput;
    }
    @Override
    public byte[] encode(byte[] input) {
        return Base64.encodeBase64(input, chunkOutput);
    }
}
