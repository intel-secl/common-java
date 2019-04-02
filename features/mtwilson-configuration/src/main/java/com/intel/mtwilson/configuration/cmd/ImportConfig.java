/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author jbuhacoff
 */
public class ImportConfig extends InteractiveCommand {
    private final String USAGE = "Usage: import-config <infile|--in=infile|--stdin> [--env-password=PASSWORD_VAR]";

    @Override
    public void execute(String[] args) throws Exception {

        String password;
        if( options != null && options.containsKey("env-password") ) {
            password = getNewPassword("the Server Configuration File", "env-password");
        }
        else {
            // gets environment variable MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
            password = Environment.get("PASSWORD");
            if( password == null ) {
                throw new IllegalArgumentException(USAGE);
            }
        }

        File outputFile;
        if( options != null && options.containsKey("out") ) {
            outputFile = new File(options.getString("out"));
        } else {
            MyConfiguration config = new MyConfiguration();
            outputFile = config.getConfigurationFile();
        }
        FileResource out = new FileResource(outputFile);

        String content;
        if( options != null && options.containsKey("in") ) {
            try (FileInputStream in = new FileInputStream(new File(options.getString("in")))) {
                content = IOUtils.toString(in);
            }
        } else if( options != null && options.getBoolean("stdin", false) ) {
            content = IOUtils.toString(System.in);
        } else if( args.length == 1 ) {
            String filename = args[0];
            try (FileInputStream in = new FileInputStream(new File(filename))) {
                content = IOUtils.toString(in);
            }
        } else {
            throw new IllegalArgumentException(USAGE);
        }
        
        if (Pem.isPem(content)) {
            log.warn(String.format("import-config: File contents are already encrypted\n%s", USAGE));
        }
        else {
            PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            if( !protection.isAvailable() ) {
    //            log.warn("Protection algorithm {} key length {} mode {} padding {} not available", protection.getAlgorithm(), protection.getKeyLengthBits(), protection.getMode(), protection.getPadding());
                protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            }
            PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(out, password, protection);
            encryptedFile.saveString(content);
        }
    }
}
