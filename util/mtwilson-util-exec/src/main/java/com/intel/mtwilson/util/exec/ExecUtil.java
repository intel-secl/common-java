/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

/**
 *
 * @author jbuhacoff
 */
public class ExecUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecUtil.class);
    
    /**
     * Executes given command without modifying quotes if available. 
     * @param executable
     * @param args
     * @return
     * @throws ExecuteException
     * @throws IOException 
     */
    public static Result executeQuoted(String executable, String... args) throws ExecuteException, IOException {
        CommandLine command = new CommandLine(executable);
        command.addArguments(args, false);
        return execute(command);
    }
	
	public static Result execute(String executable, String... args) throws ExecuteException, IOException {
        CommandLine command = new CommandLine(executable);
        command.addArguments(args);
        return execute(command);
    }
	
    public static Result executeQuietly(String executable, String... args) throws IOException {
        CommandLine command = new CommandLine(executable);
        command.addArguments(args);
        return executeQuietly(command);
    }
	    
    public static Result execute(CommandLine command) throws ExecuteException, IOException {
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout=new ByteArrayOutputStream();
        ByteArrayOutputStream stderr=new ByteArrayOutputStream();
        PumpStreamHandler psh=new PumpStreamHandler(stdout, stderr);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        executor.setStreamHandler(psh);
        log.debug("Executing command: {} with arguments: {}", command.getExecutable(), command.getArguments());
        int exitCode = 1;
        try {
            exitCode = executor.execute(command);
            return new Result(exitCode, stdout.toString(), stderr.toString());
        } catch (ExecuteException ee) {
            log.debug("Error while executing command: {}", command.toString(), ee);
            return new Result(exitCode, stdout.toString(), stderr.toString());
        }
    }
    
    public static Result execute(CommandLine command, Map environment) throws ExecuteException, IOException {
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout=new ByteArrayOutputStream();
        ByteArrayOutputStream stderr=new ByteArrayOutputStream();
        PumpStreamHandler psh=new PumpStreamHandler(stdout, stderr);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        executor.setStreamHandler(psh);
        log.debug("Executing command: {} with arguments: {}", command.getExecutable(), command.getArguments());
        int exitCode = 1;
        try {
            exitCode = executor.execute(command, environment);
            return new Result(exitCode, stdout.toString(), stderr.toString());
        } catch (ExecuteException ee) {
            log.error("Error while executing command: {}", command.toString(), ee);
            return new Result(exitCode, stdout.toString(), stderr.toString());
        }
    }

    public static Result executeQuietly(CommandLine command) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout=new ByteArrayOutputStream();
        ByteArrayOutputStream stderr=new ByteArrayOutputStream();
        PumpStreamHandler psh=new PumpStreamHandler(stdout, stderr);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        executor.setStreamHandler(psh);
        log.debug("Executing command quietly: {} with arguments: {}", command.getExecutable(), command.getArguments());
        try {
            int exitCode = executor.execute(command);
            Result result = new Result(exitCode, stdout.toString(), stderr.toString());
            return result;
        }
        catch(ExecuteException e) {
            log.error("Error while executing command: {}", command.getExecutable(), e);
            Result result = new Result(e.getExitValue(), stdout.toString(), stderr.toString());
            return result;
        }
    }
    
}
