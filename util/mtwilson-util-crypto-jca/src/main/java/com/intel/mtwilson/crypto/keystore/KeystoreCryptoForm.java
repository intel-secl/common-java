/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.keystore;

import static com.intel.mtwilson.crypto.jca.ByteArrayUtil.concat;
import static com.intel.mtwilson.crypto.jca.StringUtil.UTF8;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import com.intel.mtwilson.crypto.jca.LogUtil;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.util.encoders.Base64;
import java.security.MessageDigest;
import static com.intel.mtwilson.crypto.jca.ByteArrayUtil.toByteArray;
import com.intel.mtwilson.crypto.jca.Random;

/**
 *
 * @author jbuhacoff
 */
public class KeystoreCryptoForm implements CryptoForm {

    public static final String KEYSTORE_CRYPTOFORM_URI = "urn:intel:mtwilson:cryptoform:jca-keystore:pbkdf2+hkdf+hmac+sha256+aes128+aes192+cbc+pkcs7:masterKeyLengthBits=128,iterations=10000";

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeystoreCryptoForm.class);
    private static final LogUtil.Logger log = new LogUtil.Logger();
    private final int masterKeyLengthBits;
    private final int keyDerivationIterations;
    private final MessageDigest digestEngine;
    private final Random random;

    public KeystoreCryptoForm(Map<String, String> parameters) throws NoSuchAlgorithmException {
        masterKeyLengthBits = getInt(parameters.get("masterKeyLengthBits"), 128);
        keyDerivationIterations = getInt(parameters.get("iterations"), 10000);
        digestEngine = MessageDigest.getInstance("SHA-256"); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
        try {
            // the SecureRandomFactory loads the secure random generator from this module
            random = new Random(SecureRandomFactory.getInstance());
        }
        catch(NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    private int getInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Integer.valueOf(value);
    }

    @Override
    public String toURI() {
        return KEYSTORE_CRYPTOFORM_URI;
    }

    @Override
    public byte[] generateSalt() {
        return random.randomByteArray(32); // SHA256_LENGTH_BYTES
    }

    @Override
    public MasterKey deriveMasterKeyFromPasswordWithSalt(char[] password, byte[] salt) {
        byte[] masterKeyBytes = pbkdf2(masterKeyLengthBits, password, salt);
        return new KeystoreMasterKey(masterKeyBytes, digestEngine);
    }

    /**
     * Uses PBKDF2 with SHA-256 to convert input password to secret key. Java 8
     * includes PBKDF2WithHmacSHA256 but we haven't upgraded yet so using
     * BouncyCastle implementation here.
     *
     * @param salt
     * @param password
     * @return
     */
    private byte[] pbkdf2(int keyLengthBits, char[] password, byte[] salt) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(toByteArray(password), salt, keyDerivationIterations);
        KeyParameter keyParameter = (KeyParameter) gen.generateDerivedParameters(keyLengthBits);
        return keyParameter.getKey();
    }

    protected static byte[] hkdf(byte[] salt, byte[] ikm, int length, byte[] info) throws NoSuchAlgorithmException, InvalidKeyException {
        HKDFParameters params = new HKDFParameters(ikm, salt, info);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(params);
        byte[] okm = new byte[length];
        int generatedLength = hkdf.generateBytes(okm, 0, length);
        if (generatedLength != length) {
            throw new IllegalStateException(String.format("HKDF generated %d bytes, expected %d", generatedLength, length));
        }
        return okm;
    }

    public static class KeystoreMasterKey implements MasterKey {

        private final int encryptionKeyLengthBits = 128;
        private final int integrityKeyLengthBits = 256;
        private final byte[] masterKey;
        private final MessageDigest digestEngine;

        public KeystoreMasterKey(byte[] masterKey, MessageDigest digestEngine) {
            this.masterKey = masterKey;
            this.digestEngine = digestEngine;
        }

        @Override
        public EncryptionSecretKey deriveEncryptionSecretKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException {
            byte[] encryptionKeyBytes = hkdf(salt, masterKey, toBytes(encryptionKeyLengthBits), "KeyEncryptionKey".getBytes(UTF8)); // encryptionKeyLengthBits could be 128 or 192 for AES
            return new KeyEncryptionSecretKey("AES/CBC/PKCS5Padding", encryptionKeyBytes);
        }

        @Override
        public IntegritySecretKey deriveIntegritySecretKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException {
            byte[] integrityKeyBytes = hkdf(salt, masterKey, toBytes(integrityKeyLengthBits), "IntegrityKey".getBytes(UTF8)); // integrityKeyLengthBits must be 256 because of sha256 output size, we need SHA256_LENGTH_BYTES here
            return new KeystoreIntegritySecretKey(integrityKeyBytes);
        }

        private static int toBytes(int bits) {
            return bits / 8;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeystoreMasterKey)) {
                return false;
            }
            KeystoreMasterKey other = (KeystoreMasterKey) obj;
            return this.encryptionKeyLengthBits == other.encryptionKeyLengthBits
                    && this.integrityKeyLengthBits == other.integrityKeyLengthBits
                    && this.masterKey != null && other.masterKey != null
                    && Arrays.equals(masterKey, other.masterKey);
        }

        /**
         * Compares the encryptionKeyLengthBits, the integrityKeyLengthBits, and
         * Arrays.hashCode(sha256(masterKey)).
         *
         * @return
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.encryptionKeyLengthBits;
            hash = 29 * hash + this.integrityKeyLengthBits;
            hash = 29 * hash + Arrays.hashCode(digestEngine.digest(this.masterKey));
            return hash;
        }

    }

    public static class KeyEncryptionSecretKey implements EncryptionSecretKey {

        private final SecretKey secretKey;
        private final String cipherName;
        private final Random random;

        public KeyEncryptionSecretKey(String cipherName, byte[] secretKeyBytes) throws NoSuchAlgorithmException {
            String[] parts = cipherName.split("/"); // expecting algorithm/mode/padding, for example AES/CBC/PKCS5Padding
            this.cipherName = cipherName;
            this.secretKey = new SecretKeySpec(secretKeyBytes, parts[0]); // "AES" from "AES/CBC/PKCS5Padding"
            try {
                this.random = new Random(SecureRandomFactory.getInstance());
            }
            catch(NoSuchProviderException e) {
                throw new NoSuchAlgorithmException(e);
            }
        }

        @Override
        public byte[] encrypt(byte[] plaintext) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
            Cipher cipher = Cipher.getInstance(cipherName); // throws NoSuchAlgorithmException, NoSuchPaddingException
            int blockSizeBytes = cipher.getBlockSize();
            log.debug("secret key user-specified algorithm: {}", secretKey.getAlgorithm());
            log.debug("secret key size: {}", secretKey.getEncoded().length); // NOTE:  we are using 32 byte (256-bit) AES keys, this requires the unlimited JCE policy to be installed, or else this will throw an exception about IllegalKeySize (which could also be fixed by using 128-bit keys)
            log.debug("plaintext length to encrypt in bytes: {}", plaintext.length);
            byte[] iv = random.randomByteArray(blockSizeBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv)); // throws InvalidKeyException
            //byte[] iv = cipher.getIV();
            log.debug("iv length: {}   encoded: {}", iv.length, Base64.toBase64String(iv));
            byte[] ciphertext = cipher.doFinal(plaintext); // throws IllegalBlockSizeException, BadPaddingException
            log.debug("key ciphertext length: {}   encoded: {}", concat(iv, ciphertext).length, Base64.toBase64String(concat(iv, ciphertext)));
            return concat(iv, ciphertext);
        }

        @Override
        public byte[] decrypt(byte[] ciphertext) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
            Cipher cipher = Cipher.getInstance(cipherName); // throws NoSuchAlgorithmException, NoSuchPaddingException
            int blockSizeBytes = cipher.getBlockSize();
            log.debug("key ciphertext to decrypt length: {}  encoded: {}", ciphertext.length, Base64.toBase64String(ciphertext));
            log.debug("cipher name: {}  block size: {}", cipherName, blockSizeBytes);
            byte[] iv = Arrays.copyOfRange(ciphertext, 0, blockSizeBytes);
            log.debug("iv length: {}   encoded: {}", iv.length, Base64.toBase64String(iv));
            //cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ciphertext, 0, AES_BLOCK_SIZE_BYTES)); // throws InvalidKeyException, InvalidAlgorithmParameterException
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(ciphertext, blockSizeBytes, ciphertext.length - blockSizeBytes); // skip the first 16 bytes (IV) ,   throws IllegalBlockSizeException, BadPaddingException
        }

    }

    public static class KeystoreIntegritySecretKey implements IntegritySecretKey {

        private final byte[] secretKey;

        public KeystoreIntegritySecretKey(byte[] secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public byte[] authenticate(byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
            return hmac(secretKey, message);
        }

        /**
         * Format: hmac-sha256( alias-length, alias-utf8, key-ciphertext-length,
         * key-ciphertext, info-plaintext-length, info-plaintext ); All length
         * values are 4 bytes
         *
         * @param alias
         * @param keyCiphertextOrPemPlaintext
         * @param infoPlaintext
         * @return
         * @throws InvalidKeyException
         * @throws NoSuchAlgorithmException
         */
        private static byte[] hmac(byte[] hmacKey, byte[] message) throws InvalidKeyException, NoSuchAlgorithmException {
            SecretKeySpec key = new SecretKeySpec(hmacKey, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256"); // a string like "HmacSHA256"
            mac.init(key);
            return mac.doFinal(message);
        }

    }

    /**
     * Always with UTF-8 encoding
     *
     * @param text
     * @return
     */
    private static byte[] sha256(String text) {
        SHA256Digest digest = new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(text.getBytes(UTF8), 0, text.length());
        int digestLength = digest.doFinal(result, 0);
        if (digestLength != result.length) {
            throw new IllegalStateException("Digest length incorrect");
        }
        return result;
    }


}
