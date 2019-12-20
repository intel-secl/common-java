/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * OpenSSL commands to reproduce:
 * 
 * <pre>
# create signing key
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -inform pem -pubout -out public.pem -outform pem 

# create example content
echo '{"foo":"bar"}' > content

# create digest and signature (don't know what is the default scheme)
openssl dgst -sha256 -hex content
openssl dgst -sha256 -binary -out content.sha256 content
openssl dgst -sha256 -sign private.pem -out content.sig -keyform pem content 

# verify the signature
openssl dgst -sha256 -verify public.pem -signature content.sig -keyform pem content 

# expected result:  Verified OK  (exit code 0)

# create pss signature based on the content hash
openssl pkeyutl -in content.sha256 -out content.pss -inkey private.pem -keyform pem -sign -pkeyopt digest:sha256 -pkeyopt rsa_padding_mode:pss -pkeyopt rsa_pss_saltlen:32

# verify pss signature 
openssl pkeyutl -in content.sha256 -sigfile content.pss -inkey public.pem -pubin -keyform pem -verify -pkeyopt digest:sha256 -pkeyopt rsa_padding_mode:pss -pkeyopt rsa_pss_saltlen:32

# expected result:  Signature Verified Successfully  (with exit code 1 which is non-standard ... so have to look for this exact text)

 * </pre>
 * @author jbuhacoff
 */
public class RsaSignatureTest {
    private Logger log = LoggerFactory.getLogger(getClass());

    private byte[] generateContent() {
        return "hello".getBytes(Charset.forName("UTF-8"));
    }
    
    @BeforeClass
    public static void initProvider() {
        Security.addProvider(new BouncyCastleProvider());        
    }
    
    @Test
    public void testPSS() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        
        // initialize signature authority
        KeyPair keypair = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        Signature rsa = Signature.getInstance("SHA256withRSA/PSS", "BC"); // throws NoSuchProviderException
        rsa.initSign(keypair.getPrivate(), RandomUtil.getSecureRandom()); // throws InvalidKeyException
        //AlgorithmParameters algorithmParameters = dsa.getParameters();
         PSSParameterSpec pss = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1); // 1 is 0xbc , see openssl documentation
         rsa.setParameter(pss); // throws InvalidAlgorithmParameterException
        // prepare content for signature
        byte[] content = generateContent();
        byte[] digest = Sha256Digest.digestOf(content).toByteArray();
        
        // sign
        rsa.update(content); // throws SignatureException
        byte[] signature = rsa.sign();
        log.debug("Genereated signature: {}", Base64.encodeBase64String(signature));
        
        // verify
        rsa.initVerify(keypair.getPublic());
        rsa.update(content);
        boolean isVerified = rsa.verify(signature);
        
        // print content digest, signature, and public key to test the verification with openssl:
        log.debug("To verify with OpenSSL:\n"
                +"echo {} | base64 -d > content.sha256\n"
                +"echo {} | base64 -d > content.pss\n"
                +"echo {} | base64 -d |  openssl rsa -pubin -inform der -pubout -out public.pem -outform pem\n"
                +"openssl pkeyutl -in content.sha256 -sigfile content.pss -inkey public.pem -pubin -keyform pem -verify -pkeyopt digest:sha256 -pkeyopt rsa_padding_mode:pss -pkeyopt rsa_pss_saltlen:32",
                Base64.encodeBase64String(digest),
                Base64.encodeBase64String(signature),
                Base64.encodeBase64String(keypair.getPublic().getEncoded()));
        log.debug("digest:\n{}", Base64.encodeBase64String(digest));
        log.debug("signature:\n{}", Base64.encodeBase64String(signature));
        log.debug("public key:\n{}", Base64.encodeBase64String(keypair.getPublic().getEncoded()));
                
        log.debug("Verified? {}", isVerified);
    }
    
    private byte[] readTestResource(String path) throws IOException {
        try(InputStream in = getClass().getResourceAsStream(path)) {
            return IOUtils.toByteArray(in);
        }
    }
    
    @Test
    public void testVerifyFromOpensslPSS() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, CryptographyException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException {
        // read content and signature
        byte[] content = readTestResource("/pss/content.json");
        byte[] signature = readTestResource("/pss/content.sig");
//        byte[] digest = Sha256Digest.digestOf(content).toByteArray();
        
        // read public key 
        String publicKeyPem = new String(readTestResource("/pss/public.pem"), "UTF-8");
        PublicKey publicKey = RsaUtil.decodePemPublicKey(publicKeyPem); // throws CryptographyException
        
        // initialize verifier
        Signature rsa = Signature.getInstance("SHA256withRSA/PSS", "BC"); // throws NoSuchAlgorithmException, NoSuchProviderException
        rsa.initVerify(publicKey); // throws InvalidKeyException
         PSSParameterSpec pss = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1); // 1 is 0xbc , see openssl documentation
         rsa.setParameter(pss); // throws InvalidAlgorithmParameterException
        rsa.update(content); // throws SignatureException
        boolean isVerified = rsa.verify(signature);
        log.debug("Verified? {}", isVerified);
        
    }
}
