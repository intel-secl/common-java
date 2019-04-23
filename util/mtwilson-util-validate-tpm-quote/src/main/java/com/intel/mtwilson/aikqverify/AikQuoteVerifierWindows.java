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

public class AikQuoteVerifierWindows {

    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifierWindows.class);
    private static final int SHA1_SIZE = 20;
    private static final int MAX_NUM_OF_PCRS = 24;

    public String verifyAIKQuoteWindows(byte[] challenge, byte[] quoteFileContent, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {


        byte[] quote;
        byte[] pcrs;
        byte[] signature;
        byte[] chalmd;
        byte[] qinfoDigest;
        byte[] quoteNonce;
        byte[] pcrSelect;
        byte[] compositeHash;
        byte[] pcrConcat = new byte[SHA1_SIZE * MAX_NUM_OF_PCRS];
        byte[] pcrComposite;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int selectLength;
        int pcrLength;
        int pcr;
        int pcrBytesIndex = 0;
        int concatLength = 0;
        int numOfPCRNotIncluded = 0;
        String pcrValues = "";

        chalmd = messageDigest.digest(challenge);
        log.debug("Challenge message digest: {}", chalmd);


        int headerLength = ByteBuffer.wrap(quoteFileContent, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Header length: {}", headerLength);
        pcrLength = ByteBuffer.wrap(quoteFileContent, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("PCR length: {}", pcrLength);
        int quoteLength = ByteBuffer.wrap(quoteFileContent, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Quote length extracted: {}", quoteLength);
        int signatureLength = ByteBuffer.wrap(quoteFileContent, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Signature length: {}", signatureLength);
        pcrs = Arrays.copyOfRange(quoteFileContent, headerLength, headerLength + pcrLength);

        log.debug("PCR info: {}", pcrs);

        quote = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength, headerLength + pcrLength + quoteLength);
        log.debug("Quote: {}", quote);

        quoteNonce = Arrays.copyOfRange(quote, 6, 26);

        log.debug("Quote nonce read from file");

        if (!Arrays.equals(chalmd, quoteNonce)) {
            log.error("Nonce in quote does not match nonce in challenge file");
            throw new IOException("Nonce in quote does not match nonce in challenge file");
        }
        qinfoDigest = messageDigest.digest(quote);

        log.debug("Quote digest: {}", qinfoDigest);

        signature = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength + quoteLength, headerLength + pcrLength + quoteLength + signatureLength);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        byte[] decryptedSignature = cipher.doFinal(signature);

        log.debug("Signature has been decrypted");

        if (!Arrays.equals(qinfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - qinfoDigest.length, decryptedSignature.length))) {
            log.error("Quote verification failed");
            throw new IOException("Quote verification failed");
        }

        selectLength = ByteBuffer.wrap(quote, 6 + 20, 2).getShort();
        log.debug("Select length: {}", selectLength);
        pcrSelect = Arrays.copyOfRange(quote, 6 + 20 + 2, 6 + 20 + 2 + selectLength);

        log.debug("PCR select: {}", pcrSelect);

        compositeHash = Arrays.copyOfRange(quote, 6 + 20 + 2 + selectLength + 1, 6 + 20 + 2 + selectLength + 1 + 20);

        log.debug("Composite hash has been created");

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
        baos.write(Arrays.copyOfRange(quote, 26, 26 + 2 + selectLength));
        baos.write(bb.array());
        baos.write(pcrConcat);
        pcrComposite = baos.toByteArray();

        log.debug("PCR composite: {}", pcrComposite);

        byte[] pcrDigest = messageDigest.digest(Arrays.copyOfRange(pcrComposite, 0, 2 + selectLength + 4 + concatLength));

        log.debug("PCR digest: {}", pcrDigest);

        if (!Arrays.equals(pcrDigest, compositeHash)) {
            log.error("PCR digest verification failed");
            throw new IOException("PCR digest verification failed");
        }

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
