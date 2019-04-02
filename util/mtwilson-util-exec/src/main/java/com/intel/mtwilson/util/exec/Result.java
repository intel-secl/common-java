/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.exec;

/**
 *
 * @author jbuhacoff
 */
public class Result {
    private int exitCode;
    private String stdout;
    private String stderr;

    public Result(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    public int getExitCode() {
        return exitCode;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStdout() {
        return stdout;
    }
    
    
}
