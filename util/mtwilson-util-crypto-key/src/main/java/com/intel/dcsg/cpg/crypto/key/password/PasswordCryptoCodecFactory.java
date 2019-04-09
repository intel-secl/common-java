/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key.password;

/**
 *
 * @author jbuhacoff
 */
public class PasswordCryptoCodecFactory {
    
    public static enum KeyAlgorithmInfo {
        PBEWithMD5AndDES("PBEWithMD5AndDES",56,8,524288,true),
        PBEWithMD5AndTripleDES("PBEWithMD5AndTripleDES",168,8,2097152,true),
        PBEWithSHA1AndDESede("PBEWithSHA1AndDESede",168,8,1048576,true),
        PBKDF2WithHMACSHA1("PBKDF2WithHMACSHA1",256,16,1,false);   //  currently not supported because in the cipher code, when we create the cipher we then need to request an "AES" cipher, which is a different name, and has different initializatino code, and an IV that needs to be stored along with the salt...
        private String keyAlgorithm;
        private int keybits;
        private int saltbytes;
        private int iterations;
        private boolean isEncryptionAlgorithm; // true for key algorithms that are also valid as input to Cipher  which is the PBE* algorithms
        KeyAlgorithmInfo(String keyAlgorithm, int keybits, int saltbytes, int iterations, boolean isEncryptionAlgorithm) {
            this.keyAlgorithm = keyAlgorithm;
            this.keybits = keybits;
            this.saltbytes = saltbytes;
            this.iterations = iterations;
            this.isEncryptionAlgorithm = isEncryptionAlgorithm;
        }
        public int getKeyBits() { return keybits; }
        public int getSaltBytes() { return saltbytes; }
        public int getIterations() { return iterations; }
        public String getKeyAlgorithm() { return keyAlgorithm; }
        public boolean isEncryptionAlgorithm() { return isEncryptionAlgorithm; }
    }
    public static enum EncryptionAlgorithmInfo {
        AES("AES", 0, 16, false), // zero indicates here we're not setting a key length  but block size is known to be 16
        AES128("AES", 128, 16, false),
        AES192("AES", 192, 16, false),
        AES256("AES", 256, 16, false),
        DES("DES", 56, 8, false),
        DESede("DESede", 168, 8, false),
        TripleDES("DESede", 168, 8, false),
        PBEWithMD5AndDES("PBEWithMD5AndDES",56, 8, true),
        PBEWithMD5AndTripleDES("PBEWithMD5AndTripleDES",168, 8, true),
        PBEWithSHA1AndDESede("PBEWithSHA1AndDESede",168, 8, true);
        private String cipherAlgorithm;
        private int keybits;
        private int blocksize;
        private boolean isKeyAlgorithm; // PBEWithMD5AndDES  etc
        EncryptionAlgorithmInfo(String cipherAlgorithm, int keybits, int blocksize, boolean isKeyAlgorithm) {
            this.cipherAlgorithm = cipherAlgorithm;
            this.keybits = keybits;
            this.blocksize = blocksize;
            this.isKeyAlgorithm = isKeyAlgorithm;
        }
        public int getKeyBits() { return keybits; }
        public int getBlockSize() { return blocksize; } // in bytes.  for example AES always has 16 bytes block size and DES/3DES always has 8 bytes blocksize 
        public String getCipherAlgorithm() { return cipherAlgorithm; }
        public boolean isKeyAlgorithm() { return isKeyAlgorithm; } 
    }
    
    /**
     * Precondition:  the keyAlgorithm variable is set to the algorithm name such as "PBEWithMD5AndDES"
     * Looping through the known enum values instead of using valueOf so that we can
     * return null instead of throwing an exception when it is not found.
     * @return a KeyAlgorithmInfo value or null if the current keyAlgorithm was not found in the list
     */
    public static KeyAlgorithmInfo getKeyAlgorithmInfo(String keyAlgorithm) {
        for(KeyAlgorithmInfo instance : KeyAlgorithmInfo.values()) {
            if( instance.name().equalsIgnoreCase(keyAlgorithm) ) {
                return instance;
            }
        }
        return null;
    }
    
    /**
     * Precondition:  the keyAlgorithm variable is set to the algorithm name such as "PBEWithMD5AndDES" or "AES"
     * Looping through the known enum values instead of using valueOf so that we can
     * return null instead of throwing an exception when it is not found.
     * @return a EncryptionAlgorithmInfo value or null if the current encryptionAlgorithm was not found in the list
     */
    public static EncryptionAlgorithmInfo getEncryptionAlgorithmInfo(String encryptionAlgorithm) {
        for(EncryptionAlgorithmInfo instance : EncryptionAlgorithmInfo.values()) {
            if( instance.name().equalsIgnoreCase(encryptionAlgorithm) ) {
                return instance;
            }
        }
        return null;
    }
    
    public static CryptoCodec createCodec(String password, PasswordProtection protection) {
        if( protection.getKeyAlgorithm().startsWith("PBEWith") ) {
            PBECryptoCodec codec = new PBECryptoCodec(password, protection);
            return codec;
        }
        if( protection.getKeyAlgorithm().startsWith("PBKDF2With") ) {
            PBKDFCryptoCodec codec = new PBKDFCryptoCodec(password, protection);
            return codec;
        }
        throw new UnsupportedOperationException("Unsupported key algorithm: "+protection.getKeyAlgorithm());
    }
}
