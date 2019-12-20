/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 *
 * @author jbuhacoff
 */
public class ByteArrayOutput implements Output {
    private final ByteArrayOutputStream stdout;
    private final ByteArrayOutputStream stderr;

    public ByteArrayOutput() {
        this.stdout = new ByteArrayOutputStream();
        this.stderr = new ByteArrayOutputStream();
    }
    
    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        stdout.reset();
        return stdout;
    }
    
    @Override
    public OutputStream getErrorStream() throws FileNotFoundException {
        stderr.reset();
        return stderr;
    }
    
    public byte[] getOutputBytes() {
        return stdout.toByteArray();
    }
    
    public byte[] getErrorBytes() {
        return stderr.toByteArray();
    }
}
