/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.file.PemKeyEncryption;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeFactory;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeOpener;
import com.intel.dcsg.cpg.x509.X509Builder;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import static org.junit.Assert.*;
import org.junit.Test;
import java.security.spec.*;
import java.security.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
/**
 *
 * @author jbuhacoff
 */
public class RsaKeyEnvelopeTest {
    @Test
    public void testRsaKeyEnvelopeExample() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the rsa credential that will be used to seal & unseal the envelope
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test", keyPair).build();
        RsaCredentialX509 rsa = new RsaCredentialX509(keyPair.getPrivate(), certificate);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeFactory factory = new RsaPublicKeyProtectedPemKeyEnvelopeFactory(certificate);
        PemKeyEncryption envelope = factory.seal(secretKey);
        // unseal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeOpener recipient = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(rsa);
        Key unwrappedKey = recipient.unseal(envelope);
        // check that we got the same key back
        assertEquals(secretKey.getAlgorithm(), unwrappedKey.getAlgorithm());
        assertTrue(Arrays.equals(secretKey.getEncoded(), unwrappedKey.getEncoded()));
    }
    
    @Test(expected=CryptographyException.class)
    public void testRsaKeyEnvelopeWithWrongPrivateKey() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the rsa credential that will be used to seal the envelope
        KeyPair keyPair1 = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate1 = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test1", keyPair1).build();
        RsaCredentialX509 rsa1 = new RsaCredentialX509(keyPair1.getPrivate(), certificate1);
        // create the other rsa credential that will be used (unsuccessfully) to unseal the envelope
        KeyPair keyPair2 = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate2 = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test2", keyPair2).build();
        RsaCredentialX509 rsa2 = new RsaCredentialX509(keyPair2.getPrivate(), certificate2);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeFactory factory = new RsaPublicKeyProtectedPemKeyEnvelopeFactory(certificate1);
        PemKeyEncryption envelope = factory.seal(secretKey);
        // try to unseal the secret key with the wrong private key
        RsaPublicKeyProtectedPemKeyEnvelopeOpener recipient = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(rsa2);
        Key unwrappedKey = recipient.unseal(envelope); // throws CryptographyException: IllegalArgumentException: RsaKeyEnvelope created with md5-hash-of-rsa1-certificate cannot be unsealed with private key corresponding to md5-hash-of-rsa2-certificate
    }
    
    @Test
    public void testCipherWrapKey() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, CryptographyException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String inputStr = "TestInput";
        String keyString = "-----BEGIN PUBLIC KEY-----\n"
                + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2/AL6+f24k+gdgz1qh9h\n"
                + "uvsS3IXVpT2f7W2GzDczVnS28ygJ+pkQaVrd5drvcDy8fh/uikQqxV9R4aj596oU\n"
                + "vmJhhWmW9J8mlYj1tl5KsA1vAePBiBJJqXmzsx+EsHh0VbbvgzA9Ijdc4IiRku5E\n"
                + "kq07xunIXLjE2FUgy5MAB4H/ossc9D5lMIIlB6OMocL2N0aBmRaGEVkUNVGX6koZ\n"
                + "kWMrThNvHDey09SaihLQq4et5HEvvGGZ+a+t8x1wfhOSjXBJC1uAGSW1vw84wG3M\n"
                + "8+gzW5zjK2PaUvOM3B3OP/+gBltxaX+48sioktLzTS/xX4EtW3hP9PAreOgWU+Kb\n"
                + "3QIDAQAB\n"
                + "-----END PUBLIC KEY-----";
        
        
        PublicKey publicKey = RsaUtil.decodePemPublicKey(keyString);
        SecretKey secretKey = new SecretKeySpec(inputStr.getBytes(), 0, inputStr.length(), "AES");
        System.out.println("Secret key : " + new String(secretKey.getEncoded()));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
        
        cipher.init(Cipher.WRAP_MODE, publicKey, oaepParams); // InvalidKeyException
        byte[] encryptedKey = cipher.wrap(secretKey);
        //.doFinal("Harshitha".getBytes()); // IllegalBlockSizeException, BadPaddingException
        System.out.println("encrypted text : " + Base64.encodeBase64String(encryptedKey));
    }
}
