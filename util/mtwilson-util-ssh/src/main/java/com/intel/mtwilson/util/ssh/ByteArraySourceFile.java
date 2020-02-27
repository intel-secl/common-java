/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.ssh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.schmizz.sshj.xfer.InMemorySourceFile;

/**
 *
 * @author jbuhacoff
 */
public class ByteArraySourceFile extends InMemorySourceFile {
    private final String name;
    private final byte[] content;

    public ByteArraySourceFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getLength() {
        return content.length;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }
    
}
