/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.aikqverify;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

/**
 *
 * @author arijitgh
 */

/* AIK quote verifier for TPM 1.2 Linux
 *
 * The signed Quote received from TA is verified by the verification service for authenticity and then
 * the list of selected PCRs and their values are extracted from the quote.
 *
 */
public class AikQuoteVerifier {
    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifier.class);

    public String verifyAIKQuote(byte[] challenge, byte[] quote, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {

        byte[] pcrs;
        byte[] signature;
        byte[] select;
        byte[] challengeMessageDigest;
        //Quote info contains 8 bytes of header info, 20 bytes of quote digest and 20 bytes of challenge (nonce) digest
        byte[] quoteInfo = new byte[8 + 20 + 20];
        byte[] quoteInfoDigest;
        byte[] decryptedSignature;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int selectLength;
        int pcrLength;
        int pcr;
        int pcrIndex = 0;
        String pcrValues = "";

        //Get digest of nonce
        challengeMessageDigest = messageDigest.digest(challenge);
        log.debug("Challenge message digest: {}", challengeMessageDigest);

        //Check if quote info has PCR select info
        if (quote.length < 2) {
            log.error("AIK quote file provided has incorrect format");
            throw new IllegalStateException("AIK quote file provided has incorrect format");
        }
        //Get PCR select info length
        selectLength = ByteBuffer.wrap(quote, 0, 2).getShort();
        log.debug("Select length: {}", selectLength);
        //Get PCR select information
        select = Arrays.copyOfRange(quote, 2, 2 + selectLength);
        log.debug("Select: {}", select);
        //Check if quote from file contains PCR length info
        if (2 + selectLength + 4 > quote.length) {
            log.error("AIK quote file has incorrect quote");
            throw new IllegalStateException("AIK quote file has incorrect quote");
        }
        pcrLength = ByteBuffer.wrap(quote, 2 + selectLength, 4).getInt();
        log.debug("PCR length: {}", pcrLength);

        //Check if quote from file contains PCR list
        if (2 + selectLength + 4 + pcrLength + 20 > quote.length) {
            log.error("AIK quote file has incorrect quote");
            throw new IllegalStateException("AIK quote file has incorrect quote");
        }
        //Extract PCR list from quote
        pcrs = Arrays.copyOfRange(quote, 2 + selectLength + 4, 2 + selectLength + 4 + pcrLength);

        log.debug("PCRs: {}", pcrs);

        signature = Arrays.copyOfRange(quote, 2 + selectLength + 4 + pcrLength, quote.length);

        log.info("Signature extracted from quote");
        log.debug("Signature length: {}", signature.length);

        //Construct the quote info (First 8 bytes in quote info contain static values)
        quoteInfo[0] = 1;
        quoteInfo[1] = 1;
        quoteInfo[2] = 0;
        quoteInfo[3] = 0;
        quoteInfo[4] = 'Q';
        quoteInfo[5] = 'U';
        quoteInfo[6] = 'O';
        quoteInfo[7] = 'T';
        byte[] quoteWithoutSignature = Arrays.copyOfRange(quote, 0, 2 + selectLength + 4 + pcrLength);
        messageDigest.update(quoteWithoutSignature);
        byteArrayOutputStream.write(Arrays.copyOfRange(quoteInfo, 0, 8));
        //Add quote digest read from file to quote info
        byteArrayOutputStream.write(messageDigest.digest());
        //Add challenge digest to quote info
        byteArrayOutputStream.write(challengeMessageDigest);
        quoteInfo = byteArrayOutputStream.toByteArray();

        log.info("Quote info created");
        log.debug("Quote info: {}", quoteInfo);

        quoteInfoDigest = messageDigest.digest(quoteInfo);

        log.debug("Quote Digest: {}", quoteInfoDigest);

        //Decrypt file using RSA public key provided
        Cipher cipher = Cipher.getInstance("RSA");
        try {
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            decryptedSignature = cipher.doFinal(signature);
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException exc) {
            log.error("Error decrypting signature extracted from quote");
            throw new IllegalStateException("Error decrypting signature extracted from quote ", exc);
        }

        log.info("Signature has been decrypted");

        /*Check if the digest of the constructed quote info matches that of the digest in the decrypted signature
         * Decrypted signature contains signature header which is excluded from verification, only the digest is extracted
         * from signature
         */
        if (!Arrays.equals(quoteInfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - quoteInfoDigest.length, decryptedSignature.length))) {
            log.error("Quote verification failed");
            throw new IllegalStateException("Quote verification failed");
        }
        log.info("Quote verification is successful");
        //Get the PCR values from quote based on PCR select info
        for (pcr = 0; pcr < 8 * selectLength; pcr++) {
            if ((select[pcr / 8] & (1 << (pcr % 8))) != 0) {
                pcrValues = pcrValues + String.format("%2d", pcr) + " ";
                for (int i = 0; i < 20; i++) {
                    pcrValues = pcrValues + String.format("%02x", pcrs[20 * pcrIndex + i]);
                }
                pcrValues = pcrValues + "\n";
                pcrIndex++;
            }
        }
        log.debug("aikqverify output:\n {}", pcrValues);
        return pcrValues;
    }
}