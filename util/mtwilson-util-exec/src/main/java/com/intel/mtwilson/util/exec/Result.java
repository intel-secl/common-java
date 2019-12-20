/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.exec;
import java.nio.charset.Charset;

/**
 *
 * @author jbuhacoff
 */
public class Result {
    final private int exitCode;
    final private byte[] stdout;
    final private byte[] stderr;

    public Result(int exitCode, byte[] stdout, byte[] stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    public int getExitCode() {
        return exitCode;
    }

    public String getStderr() {
        return new String(stderr, Charset.forName("UTF-8"));
    }

    public String getStdout() {
        return new String(stdout, Charset.forName("UTF-8"));
    }
    
    public byte[] getStderrByteArray() {
        return stderr;
    }
    
    public byte[] getStdoutByteArray() {
        return stdout;
    }
}
