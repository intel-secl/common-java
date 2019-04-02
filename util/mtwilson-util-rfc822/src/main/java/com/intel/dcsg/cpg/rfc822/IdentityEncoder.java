/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;

/**
 * The "identity" encoding is a no-op.
 * 
 * @author jbuhacoff
 */
public class IdentityEncoder implements Encoder {

    @Override
    public byte[] encode(byte[] data) throws IOException {
        return data;
    }
    
}
