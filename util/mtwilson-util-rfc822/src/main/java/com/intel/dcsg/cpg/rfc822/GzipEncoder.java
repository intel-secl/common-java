/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author jbuhacoff
 */
public class GzipEncoder implements Encoder {
    @Override
    public byte[] encode(byte[] input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(buffer); // throws IOException
        out.write(input);
        out.close();
        return buffer.toByteArray();
    }
}
