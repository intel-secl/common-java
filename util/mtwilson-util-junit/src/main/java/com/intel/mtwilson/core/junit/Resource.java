/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.junit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class Resource {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static String toString(String filename) throws IOException {
        if (filename == null) {
            throw new NullPointerException();
        }
        try (InputStream in = Resource.class.getResourceAsStream(String.format("/%s", filename))) {
            return IOUtils.toString(in, UTF8);
        }
    }

    public static String toString(String filename, Charset charset) throws IOException {
        if (filename == null) {
            throw new NullPointerException();
        }
        try (InputStream in = Resource.class.getResourceAsStream(String.format("/%s", filename))) {
            return IOUtils.toString(in, charset);
        }
    }
    
    public static byte[] toByteArray(String filename) throws IOException {
        if (filename == null) {
            throw new NullPointerException();
        }
        try (InputStream in = Resource.class.getResourceAsStream(String.format("/%s", filename))) {
            return IOUtils.toByteArray(in);
        }
    }

    

}
