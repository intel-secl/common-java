/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


    /*
     File stdoutLogFile = new File(taskDirectoryPath + File.separator + String.valueOf(commandId) + ".stdout.log");
     File stderrLogFile = new File(taskDirectoryPath + File.separator + String.valueOf(commandId) + ".stderr.log");
     * 
     */

/**
 *
 * @author jbuhacoff
 */
public class FileOutput implements Output {
    private final File stdout;
    private final File stderr;

    public FileOutput(File stdout, File stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(stdout);
    }
    
    @Override
    public OutputStream getErrorStream() throws FileNotFoundException {
        return new FileOutputStream(stderr);
    }
    
    public File getOutputFile() {
        return stdout;
    }
    
    public File getErrorFile() {
        return stderr;
    }
}
