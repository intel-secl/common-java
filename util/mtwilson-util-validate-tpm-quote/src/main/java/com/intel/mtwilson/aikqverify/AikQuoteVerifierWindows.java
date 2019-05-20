/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.aikqverify;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.util.Arrays;

/**
 *
 * @author arijitgh
 */

/* AIK quote verifier for TPM 1.2 Windows
 *
 * The signed Quote received from TA is verified by the verification service for authenticity and then
 * the list of selected PCRs and their values are extracted from the quote.
 *
 */
public class AikQuoteVerifierWindows {

    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifierWindows.class);
    private static final int SHA1_SIZE = 20;
    //Number of PCRs in quote is 24
    private static final int MAX_NUM_OF_PCRS = 24;

    public String verifyAIKQuoteWindows(byte[] challenge, byte[] quoteFileContent, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {


        byte[] quote;
        byte[] pcrs;
        byte[] signature;
        byte[] challengeMessageDigest;
        byte[] quoteInfoDigest;
        byte[] quoteNonce;
        byte[] pcrSelect;
        byte[] compositeHash;
        byte[] pcrConcat = new byte[SHA1_SIZE * MAX_NUM_OF_PCRS];
        byte[] pcrComposite;
        byte[] decryptedSignature;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int selectLength;
        int pcrLength;
        int pcr;
        int pcrBytesIndex = 0;
        int concatLength = 0;
        int numOfPCRNotIncluded = 0;
        String pcrValues = "";

        //Get digest of nonce
        challengeMessageDigest = messageDigest.digest(challenge);
        log.debug("Challenge message digest: {}", challengeMessageDigest);

        /*Quote Header structure:
            Magic              ----> 4 bytes
            Platform           ----> 4 bytes
            HeaderSize         ----> 4 bytes
            PcrValues size     ----> 4 bytes
            Quote length       ----> 4 bytes
            Signature size     ----> 4 bytes
            Log size           ----> 4 bytes
          */

        //Get HeaderSize from quote header
        int headerLength = ByteBuffer.wrap(quoteFileContent, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Header length: {}", headerLength);
        //Get PcrValues size from quote header
        pcrLength = ByteBuffer.wrap(quoteFileContent, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("PCR length: {}", pcrLength);
        //Get Quote length from quote header
        int quoteLength = ByteBuffer.wrap(quoteFileContent, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Quote length extracted: {}", quoteLength);
        //Get Signature size from quote header
        int signatureLength = ByteBuffer.wrap(quoteFileContent, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Signature length: {}", signatureLength);
        //Get the list of PCRs from quote
        pcrs = Arrays.copyOfRange(quoteFileContent, headerLength, headerLength + pcrLength);

        log.debug("PCR info: {}", pcrs);

        quote = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength, headerLength + pcrLength + quoteLength);
        log.debug("Quote: {}", quote);

        //Get nonce of size 20 bytes starting from 6th byte till 26th byte
        quoteNonce = Arrays.copyOfRange(quote, 6, 26);

        log.info("Quote nonce read from file");

        //Check if challenge digest in file matches the same in quote
        if (!Arrays.equals(challengeMessageDigest, quoteNonce)) {
            log.error("Nonce in quote does not match nonce in file");
            throw new IllegalStateException("Nonce in quote does not match nonce in file");
        }
        quoteInfoDigest = messageDigest.digest(quote);

        log.debug("Quote digest: {}", quoteInfoDigest);

        //Decrypt the signature in quote and extract the quote digest for verification
        signature = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength + quoteLength, headerLength + pcrLength + quoteLength + signatureLength);
        Cipher cipher = Cipher.getInstance("RSA");
        try {
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            decryptedSignature = cipher.doFinal(signature);
        } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException exc) {
            log.error("Error decrypting signature extracted from quote");
            throw new IllegalStateException("Error decrypting signature extracted from quote ", exc);
        }

        log.debug("Signature has been decrypted");
        //Check if quote info digest matches quote digest extracted from signature
        if (!Arrays.equals(quoteInfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - quoteInfoDigest.length, decryptedSignature.length))) {
            log.error("Quote verification failed");
            throw new IllegalStateException("Quote verification failed");
        }
        log.info("Quote info digest has been verified with the digest extracted from signature");
        selectLength = ByteBuffer.wrap(quote, 6 + 20, 2).getShort();
        log.debug("Select length: {}", selectLength);
        pcrSelect = Arrays.copyOfRange(quote, 6 + 20 + 2, 6 + 20 + 2 + selectLength);

        log.debug("PCR select: {}", pcrSelect);

        compositeHash = Arrays.copyOfRange(quote, 6 + 20 + 2 + selectLength + 1, 6 + 20 + 2 + selectLength + 1 + 20);

        log.info("Composite hash has been created");

        //Construct PCR list and get the SHA-1 hash which needs to be verified with the PCR composite hash present in quote
        for (pcr = 0; pcr < 8 * selectLength; pcr++) {
            if ((pcrSelect[pcr / 8] & (1 << (pcr % 8))) != 0) {
                if (concatLength < pcrConcat.length) {
                    for (int index = 0; index < 20; index++) {
                        pcrConcat[(pcr - numOfPCRNotIncluded) * 20 + index] = pcrs[pcr * 20 + index];
                    }
                }
                concatLength += 20;
            } else {
                numOfPCRNotIncluded++;
            }
        }

        log.debug("PCR concat: {}", pcrConcat);

        log.debug("Concat length: {}", concatLength);
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(concatLength);
        byteArrayOutputStream.write(Arrays.copyOfRange(quote, 26, 26 + 2 + selectLength));
        byteArrayOutputStream.write(bb.array());
        byteArrayOutputStream.write(pcrConcat);
        pcrComposite = byteArrayOutputStream.toByteArray();

        log.debug("PCR composite: {}", pcrComposite);

        byte[] pcrDigest = messageDigest.digest(Arrays.copyOfRange(pcrComposite, 0, 2 + selectLength + 4 + concatLength));

        log.debug("PCR digest: {}", pcrDigest);
        //Check PCR digest is same as composite hash in quote
        if (!Arrays.equals(pcrDigest, compositeHash)) {
            log.error("PCR digest verification failed");
            throw new IllegalStateException("PCR digest verification failed");
        }

        log.info("Quote verification is successful");
        for (pcr = 0; pcr < 8 * selectLength; pcr++) {
            pcrValues = pcrValues + String.format("%2d", pcr) + " ";
            while (pcrBytesIndex < pcrLength) {
                pcrValues = pcrValues + String.format("%02x", pcrs[pcrBytesIndex]);
                pcrBytesIndex++;
                if ((pcrBytesIndex) % 20 == 0)
                    break;
            }
            pcrValues = pcrValues + "\n";
        }
        log.debug("aikqverify output:\n {}", pcrValues);

        return pcrValues;
    }
}
