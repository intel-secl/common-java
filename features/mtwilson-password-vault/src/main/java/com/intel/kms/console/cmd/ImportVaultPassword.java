/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
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
import org.apache.commons.io.IOUtils;

/**
 * Imports an existing password into the password vault.
 * The password bytes will be read from stdin, if it's a string its assumed
 * to be utf8-encoded.
 *
 * How to run this command: cat password | kms import-vault-password --alias={alias}
 * 
 * Or if using echo, make sure you add -n flag to avoid incorrectly adding
 * a newline at end of password:  echo -n $PASSWORD | kms import-vault-password --alias={alias}
 *
 * A complete PEM-style envelope can be imported with the base64-encoded key by
 * providing the --pem option: cat password.pem | kms import-vault-password {alias} --pem
 *
 * Example PEM input:
 * <pre>
 * -----BEGIN PASSWORD-----
 * DrwgJGzw5C9rwpeQVkAU0TFxIu4JTTyzmeHmxcyxFaE=
 * -----END PASSWORD-----
 *</pre>
 * 
 * If the alias already exists in the keystore the import will be cancelled
 * and the command will return with a non-zero exit code. If the alias already
 * exists and the "--force" option is provided, any existing entry will be
 * overwritten.
 * 
 * Example:
 * export VAULT_PASSWORD=changeitmaster
 * echo -n changeit | kms import-vault-password --keystore=/opt/kms/configuration/password-vault.jck --storetype=JCEKS --storepassenv=VAULT_PASSWORD --alias=test1 --force
 * 
 * Copy a password:
 * export VAULT_PASSWORD=changeitmaster
 * kms export-vault-password --keystore=/opt/kms/configuration/password-vault.jck --storetype=JCEKS --storepassenv=VAULT_PASSWORD --alias=test1 --pem | kms import-vault-password --keystore=/opt/kms/configuration/password-vault.jck --storetype=JCEKS --storepassenv=VAULT_PASSWORD --alias=test2 --force

 * @author jbuhacoff
 */
public class ImportVaultPassword extends InteractiveCommand {

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

        // read password from stdin
        byte[] password;
        if (options.getBoolean("pem", false)) {
            // read base64-encoded key in PEM-style format
            Pem pem = Pem.valueOf(IOUtils.toString(System.in, Charset.forName("UTF-8")));
            password = pem.getContent();
        } else {
            password = IOUtils.toByteArray(System.in);
        }
        if( password == null ) {
            throw new IllegalArgumentException("Password is missing");
        }
        
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(configuration)) {
            if( passwordVault.contains(alias) && !options.getBoolean("force", false)) {
                throw new IllegalArgumentException("Entry already exists; choose different alias or use --force option");
            }
            passwordVault.set(alias, new Password(password, Charset.forName("UTF-8")));
        }


    }
}
