/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory.EncryptionAlgorithmInfo;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory.KeyAlgorithmInfo;

/**
 *
 * @author jbuhacoff
 */
public class PasswordProtectionBuilder extends ProtectionBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordProtectionBuilder.class);
    protected PasswordProtection passwordProtection = new PasswordProtection();

    public static PasswordProtectionBuilder factory() {
        return new PasswordProtectionBuilder();
    }

    // TODO:  need to simplify this and move all the rfc822 code to PasswordEncryptedFile where it's actually used -- we should not do that kind of microformat here, and remove the rfc822 dependency
    public PasswordProtectionBuilder keyAlgorithm(String keyAlgorithm) {
        passwordProtection.keyAlgorithm = keyAlgorithm; // for example PBKDF2WithHmacSHA1
        KeyAlgorithmInfo keyAlgInfo = PasswordCryptoCodecFactory.getKeyAlgorithmInfo(keyAlgorithm);
        if (keyAlgInfo != null) {
            log.debug("found key algorithm info, iterations {} salt bytes {}", keyAlgInfo.getIterations(), keyAlgInfo.getSaltBytes());
            if (passwordProtection.iterations == 0) {
                iterations(keyAlgInfo.getIterations());
                log.warn("Using default iteration count {}; it should be specified explicitly", passwordProtection.iterations);
            }
            if (passwordProtection.saltBytes == 0) {
                saltBytes(keyAlgInfo.getSaltBytes());
                log.warn("Using default salt bytes {}; it should be specified explicitly", passwordProtection.saltBytes);
            }
            if (super.protection.getAlgorithm() == null && keyAlgInfo.isEncryptionAlgorithm()) {
                log.debug("using default encryption algorithm info");
                algorithm(passwordProtection.keyAlgorithm);
            }
        }
        return this;
    }

    public PasswordProtectionBuilder saltBytes(int saltBytes) {
        passwordProtection.saltBytes = saltBytes;
        return this;
    }

    public PasswordProtectionBuilder iterations(int iterations) {
        passwordProtection.iterations = iterations;
        return this;
    }

    public PasswordProtectionBuilder pbkdf2WithHmacSha1() {
        passwordProtection.keyAlgorithm = "PBKDF2WithHmacSHA1";
        return this;
    }

    @Override
    public PasswordProtectionBuilder aes(int keyLengthBits) {
        super.aes(keyLengthBits);
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha1() {
        super.sha1();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha256() {
        super.sha256();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha384() {
        super.sha384();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha512() {
        super.sha512();
        return this;
    }

    @Override
    public PasswordProtectionBuilder block() {
        super.block();
        return this;
    }

    @Override
    public PasswordProtectionBuilder stream() {
        super.stream();
        return this;
    }

    @Override
    public PasswordProtectionBuilder algorithm(String algorithm) {
        super.algorithm(algorithm);
        log.debug("algorithm set to {}", protection.getAlgorithm());
        EncryptionAlgorithmInfo info = PasswordCryptoCodecFactory.getEncryptionAlgorithmInfo(protection.getAlgorithm());
        if( info != null ) {
            log.debug("found encryption alg info for {} -> {}", algorithm, protection.getAlgorithm());
            if( info.isKeyAlgorithm() ) {
                log.debug("setting encryption algorithm {}", info.getCipherAlgorithm());
                keyAlgorithm(protection.getAlgorithm()); // no mode or padding
            }
        }
        return this;
    }

    @Override
    public PasswordProtectionBuilder mode(String mode) {
        super.mode(mode);
        return this;
    }

    @Override
    public PasswordProtectionBuilder padding(String padding) {
        super.padding(padding);
        return this;
    }

    @Override
    public PasswordProtectionBuilder digestAlgorithm(String digestAlgorithm) {
        super.digestAlgorithm(digestAlgorithm);
        return this;
    }

    @Override
    public PasswordProtectionBuilder keyLengthBits(int keyLengthBits) {
        super.keyLengthBits(keyLengthBits);
        return this;
    }

    @Override
    public PasswordProtectionBuilder blockSizeBytes(int blockSizeBytes) {
        super.blockSizeBytes(blockSizeBytes);
        return this;
    }

    @Override
    public PasswordProtectionBuilder digestSizeBytes(int digestSizeBytes) {
        super.digestSizeBytes(digestSizeBytes);
        return this;
    }

    @Override
    public PasswordProtection build() {
        super.build();
        if (passwordProtection.keyAlgorithm == null) {
            throw new IllegalArgumentException("Key algorithm is missing");
        }
        passwordProtection.setAlgorithm(protection.getAlgorithm());
        passwordProtection.setMode(protection.getMode());
        passwordProtection.setPadding(protection.getPadding());
        passwordProtection.setKeyLengthBits(protection.getKeyLengthBits());
        passwordProtection.setBlockSizeBytes(protection.getBlockSizeBytes());
        passwordProtection.setDigestAlgorithm(protection.getDigestAlgorithm());
        passwordProtection.setDigestSizeBytes(protection.getDigestSizeBytes());
        passwordProtection.setCipher(protection.getCipher());
        return passwordProtection;
    }
}
