/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.exec;

import java.io.IOException;
import org.apache.commons.exec.ExecuteException;
import org.junit.Test;
import static org.junit.Assert.*;
import com.intel.dcsg.cpg.io.Platform;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
/**
 *
 * @author jbuhacoff
 */
public class ExecTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecTest.class);

    @Test
    public void testWindows() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.execute("cmd.exe", "/C", "dir");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
           assertEquals(0, result.getExitCode());
       } 
    }


    @Test
    public void testWindowsQuietFailure() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.executeQuietly("cmd.exe", "/C", "dir", "___ this path does not exist ___");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
           assertEquals(1, result.getExitCode());
       } 
    }

    @Test(expected=ExecuteException.class)
    public void testWindowsFailure() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.execute("cmd.exe", "/C", "dir", "___ this path does not exist ___");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
       } 
    }
    
    @Test
    public void testWindowsPipe() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           Result result = ExecUtil.execute("cmd.exe", "/C", "dir | echo");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout()); // "ECHO is ON"
           log.debug("stderr: {}", result.getStderr());
           assertEquals(0, result.getExitCode());
       } 
    }

    @Test
    public void testWindowsStdin() throws ExecuteException, IOException {
       if( Platform.isWindows() ) {
           ByteArrayInputStream in = new ByteArrayInputStream("hello world".getBytes(Charset.forName("UTF-8")));
           Result result = ExecUtil.execute(new HashMap<String,String>(), in, "cmd.exe", "/C", "echo");
           log.debug("exit code {}", result.getExitCode());
           log.debug("stdout: {}", result.getStdout());
           log.debug("stderr: {}", result.getStderr());
           assertEquals(0, result.getExitCode());
       } 
    }

}
