/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.launcher.console;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.ExtendedOptions;
import com.intel.dcsg.cpg.extensions.PluginRegistry;
import com.intel.dcsg.cpg.extensions.PluginRegistryFactory;
import com.intel.dcsg.cpg.util.shiro.Login;
import com.intel.mtwilson.pipe.TransformerPipe;
import com.intel.mtwilson.text.transform.CamelCaseToHyphenated;
import com.intel.mtwilson.text.transform.RegexTransformer;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class Dispatcher implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dispatcher.class);
    private String[] args;
    private int exitCode;
    private PluginRegistry<Command> registry;
    
    public Dispatcher() {
        HashMap<String,String> replacements = new HashMap<>();
        replacements.put("mt-wilson", "mtwilson");
        RegexTransformer replacing = new RegexTransformer(replacements);
        CamelCaseToHyphenated hyphenating = new CamelCaseToHyphenated();
        TransformerPipe<String> pipe = new TransformerPipe<>(hyphenating, replacing);
        // make a map of command names to class names, like "hello-world" -> "com.example.HelloWorld" and also qualified names like "com.example:hello-world" -> "com.example.HelloWorld"
        registry = PluginRegistryFactory.createRegistry(Command.class, pipe); 
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getCommandName() {
        if (args == null || args.length == 0) {
            return null;
        }
        return args[0];
    }

    public Command findCommand(String commandName) {
        return registry.lookup(commandName);
    }

    @Override
    public void run() {
        String commandName = getCommandName();
        if (commandName == null) {
            log.error("Usage: <command> [args]");
            exitCode = 1;
            return;
        }

        try {
            Command command = findCommand(commandName);
            if (command == null) {
                log.error("Unrecognized command: " + commandName);
                exitCode = 2;
            } else {
                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
                ExtendedOptions getopt = new ExtendedOptions(subargs);
                Configuration options = getopt.getOptions();
                subargs = getopt.getArguments();
                command.setOptions(options);
                
                // Here we could support an option to execute the command as a specific user
                // For now, we assume someone with shell access running commands is the superuser
                // This allows various commands to call any methods that may be annotated with
                // shiro @RequiresPermissions and for it to work;  if we don't have any 
                // logged in user at all then shiro will throw an exception like this:
                // org.apache.shiro.UnavailableSecurityManagerException: No SecurityManager accessible to the calling code, either bound to the org.apache.shiro.util.ThreadContext or as a vm static singleton.  This is an invalid application configuration.
                Login.superuser();
                
                command.execute(subargs);
                exitCode = 0;
            }
        } catch (Exception e) {
            if (e.getMessage() == null) {
                log.error("Error while executing {}: {}", commandName, e.getClass().getName());
            } else {
                log.error("Error while executing {}: {}", commandName, e.getMessage());
            }
            log.debug("Error while executing {}", commandName, e);
            exitCode = 3;
        }

    }
}
