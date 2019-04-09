/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import com.intel.dcsg.cpg.rfc822.Message;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Signature;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * Example usage:
 * 
 * KeyPair keypair = RsaUtil.generateRsaKeyPair(1024); // bits
 * RsaSignatureMessageWriter authority = new RsaSignatureMessageWriter();
 * authority.setPrivateKey(keypair.getPrivate());
 * byte[] signatureMessage = authority.write("hello world");
 * 
 * @author jbuhacoff
 */
public class RsaSignatureMessageWriter {
    private PrivateKey privateKey;
    private String signatureAlgorithm = "SHA256withRSA";
    private final Charset utf8 = Charset.forName("UTF-8");
    
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
    
    
    
    public byte[] writeString(String input) {
        return writeString(input, utf8);
    }
    
    public byte[] writeString(String input, Charset charset) {
        return write(input.getBytes(charset), "text/plain; charset=\""+charset.name()+"\"");  // charset.name() might be "UTF-8"
    }
    
    public byte[] writeByteArray(byte[] input) {
        return write(input, "application/octet-stream");
    }
    
    public byte[] writeMessage(Message input) {
        return write(input.toByteArray(), "message/rfc822");
    }
    
    public byte[] write(byte[] content, String contentType) {
        byte[] messageBytes = signature(content);
        Message message = new Message();
        message.setContent(Base64.encodeBase64Chunked(messageBytes));
        message.setContentLength(messageBytes.length); // we're using it to indicate the length of the original message (to help detect errors) - this is non-standard, the http and mime specs say that if a transfer encoding is used, then content-length must not be used (probably because they couldn't decide if it should be the length before or after encoding or because they assumed it would be streaming and length wouldn't be available)
        message.setContentType("application/signature.java; alg=\"SHA256withRSA\"; key=\"default\""); // TODO  convert from RSAwithSHA256 , add keyId parameter
        message.setContentTransferEncoding("base64");
        return message.toByteArray();
    }
    
    public byte[] signature(byte[] document) {
        try {
            Signature rsa = Signature.getInstance(signatureAlgorithm);  // throws NoSuchAlgorithmException
            rsa.initSign(privateKey); // throws InvalidKeyException
            rsa.update(document); // throws java.security.SignatureException
            return rsa.sign(); // throws java.security.SignatureException
        }
        catch(Exception e) {
            throw new SignatureException(e);
        }
    }
    
}
