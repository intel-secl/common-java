package com.intel.mtwilson.aikqverify;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

public class AikQuoteVerifier {
    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifier.class);

    public String verifyAIKQuote(byte[] challenge, byte[] quote, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        byte[] pcrs;
        byte[] signature;
        byte[] select;
        byte[] chalmd;
        byte[] qinfo = new byte[8 + 20 + 20];
        byte[] qinfoDigest;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int selectLength;
        int pcrLength;
        int pcr;
        int pcrIndex = 0;
        String pcrValues = "";

        chalmd = messageDigest.digest(challenge);
        log.debug("Challenge message digest: {}", chalmd);

        if (quote.length < 2)
            log.error("Input AIK quote file has incorrect format");
        selectLength = ByteBuffer.wrap(quote, 0, 2).getShort();
        log.debug("Select length: {}", selectLength);
        select = Arrays.copyOfRange(quote, 2, 2 + selectLength);
        log.debug("Select: {}", select);
        if (2 + selectLength + 4 > quote.length)
            log.error("Input AIK quote file has incorrect format");
        pcrLength = ByteBuffer.wrap(quote, 2 + selectLength, 4).getInt();
        log.debug("PCR length: {}", pcrLength);
        if (2 + selectLength + 4 + pcrLength + 20 > quote.length)
            log.error("Input AIK quote file has incorrect format");
        pcrs = Arrays.copyOfRange(quote, 2 + selectLength + 4, 2 + selectLength + 4 + pcrLength);

        log.debug("PCRs: {}", pcrs);

        signature = Arrays.copyOfRange(quote, 2 + selectLength + 4 + pcrLength, quote.length);

        log.debug("Signature extracted from quote");
        log.debug("Signature length: {}", signature.length);

        qinfo[0] = 1;
        qinfo[1] = 1;
        qinfo[2] = 0;
        qinfo[3] = 0;
        qinfo[4] = 'Q';
        qinfo[5] = 'U';
        qinfo[6] = 'O';
        qinfo[7] = 'T';
        byte[] quoteWithoutSignature = Arrays.copyOfRange(quote, 0, 2 + selectLength + 4 + pcrLength);
        messageDigest.update(quoteWithoutSignature);
        baos.write(Arrays.copyOfRange(qinfo, 0, 8));
        baos.write(messageDigest.digest());
        baos.write(chalmd);
        qinfo = baos.toByteArray();

        log.debug("Quote info: {}", qinfo);

        qinfoDigest = messageDigest.digest(qinfo);

        log.debug("Quote Digest: {}", qinfoDigest);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        byte[] decryptedSignature = cipher.doFinal(signature);

        log.debug("Signature has been decrypted");


        if (!Arrays.equals(qinfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - qinfoDigest.length, decryptedSignature.length))) {
            log.error("Quote verification failed");
            throw new IOException("Quote verification failed");
        }

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