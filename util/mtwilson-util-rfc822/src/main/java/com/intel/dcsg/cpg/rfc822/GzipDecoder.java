/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class GzipDecoder implements Decoder {
    @Override
    public byte[] decode(byte[] gzip) throws IOException {
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip)); // throws IOException
        byte[] plain = IOUtils.toByteArray(in);
        in.close();
        return plain;
    }
}
