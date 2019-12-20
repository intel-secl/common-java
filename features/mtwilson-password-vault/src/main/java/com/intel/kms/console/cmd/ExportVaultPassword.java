/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.console.cmd;

import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.mtwilson.core.PasswordVaultFactory;
import static com.intel.mtwilson.core.PasswordVaultFactory.PASSWORD_VAULT_FILE_PROPERTY;
import static com.intel.mtwilson.core.PasswordVaultFactory.PASSWORD_VAULT_KEY_PROPERTY;
import static com.intel.mtwilson.core.PasswordVaultFactory.PASSWORD_VAULT_KEY_PROVIDER_PROPERTY;
import static com.intel.mtwilson.core.PasswordVaultFactory.PASSWORD_VAULT_TYPE_PROPERTY;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.nio.charset.Charset;

/**
 * Exports an existing password from the password vault.
 * The default output will include only ASCII printable characters.
 * For passwords with non-printable characters, use the --pem option.
 *
 * How to run this command: kms export-vault-password {alias}
 *
 * Example output:
 * <pre>
 * CGaTpWf3YcFeEzyQfxlOAQ
 * </pre>
 *
 * A complete PEM-style envelope can be printed with the base64-encoded key by
 * providing the --pem option: kms export-vault-password {alias} --pem
 *
 * Example PEM output:
 * <pre>
 * -----BEGIN PASSWORD-----
 * DrwgJGzw5C9rwpeQVkAU0TFxIu4JTTyzmeHmxcyxFaE=
 * -----END PASSWORD-----
 * </pre>
 * 
 * Example:
 * export VAULT_PASSWORD=changeitmaster
 * kms import-vault-password --keystore=/opt/kms/configuration/password-vault.jck --storetype=JCEKS --storepassenv=VAULT_PASSWORD --alias=test1
 * 
 * @author jbuhacoff
 */
public class ExportVaultPassword extends InteractiveCommand {

    @Override
    public void execute(String[] args) throws Exception {
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        String alias = options.getString("alias");
        String keystorePath = options.getString("keystore");
        String keystoreType = options.getString("storetype");
        String keystorePassword = options.getString("storepass");
        String keystorePasswordEnv = options.getString("storepassenv");
        if( alias == null || alias.isEmpty() ) {
            throw new IllegalArgumentException("Must specify alias");
        }
        if( keystorePath != null && !keystorePath.isEmpty() ) {
            configuration.set(PASSWORD_VAULT_FILE_PROPERTY, keystorePath);
        }
        if( keystoreType != null && !keystoreType.isEmpty() ) {
            configuration.set(PASSWORD_VAULT_TYPE_PROPERTY, keystoreType);
        }
        if( keystorePassword != null && !keystorePassword.isEmpty() ) {
            configuration.set(PASSWORD_VAULT_KEY_PROPERTY, keystorePassword);
        }
        else if( keystorePasswordEnv != null && !keystorePasswordEnv.isEmpty() ) {
            configuration.set(PASSWORD_VAULT_KEY_PROPERTY, System.getenv(keystorePasswordEnv));
        }
        else {
            // load password vault key from a variable like "MTWILSON_PASSWORD"
            configuration.set(PASSWORD_VAULT_KEY_PROVIDER_PROPERTY, "environment");
        }
        
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(configuration)) {
            if( passwordVault.contains(alias)) {
                Password password = passwordVault.get(alias);
                if (this.options != null && options.getBoolean("pem", false)) {
                    // print base64-encoded key in PEM-style format
                    Pem pem = new Pem("PASSWORD", password.toByteArray(Charset.forName("UTF-8")));
                    System.out.print(pem.toString()); // the PEM document already includes a trailing newline
                } else {
                    System.out.print(String.valueOf(password.toCharArray())); // intentionally do not add a newline, so if someone enters a 5 character password "hello" they should be able to pipe this output to wc -c and see it's 5 characters, not 6
                }
            }
            else {
                throw new IllegalArgumentException("Entry not found in keystore");
            }
        }


    }
}
