package com.intel.mtwilson.aikqverify;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class AikQuoteVerifier {
    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifier.class);

    public String verifyAIKQuote(byte[] challenge, byte[] quote, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        byte[] pcrs = null;
        byte[] signature = null;
        byte[] select;
        byte[] chalmd = new byte[20];
        byte[] qinfo = new byte[8 + 20 + 20];
        byte[] qinfoDigest;
        StringBuilder sb = new StringBuilder();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int selectLength = 0;
        int pcrLength = 0;
        int pcr;
        int pcrIndex = 0;
        String pcrValues = "";


        messageDigest.update(challenge);
        chalmd = messageDigest.digest();
        messageDigest.reset();

        for (byte b : chalmd) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Challenge message digest hex: {}", sb.toString());
        sb = new StringBuilder();

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
        for (byte b : pcrs) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR hex: {}", sb.toString());
        sb = new StringBuilder();
        signature = Arrays.copyOfRange(quote, 2 + selectLength + 4 + pcrLength, quote.length);
        for (byte b : signature) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Signature hex: {}", sb.toString());
        sb = new StringBuilder();
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
        for (byte b : qinfo) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Quote info hex: {}", sb.toString());
        sb = new StringBuilder();

        messageDigest.update(qinfo);
        qinfoDigest = messageDigest.digest();
        for (byte b : qinfoDigest) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Quote Digest hex: {}", sb.toString());
        sb = new StringBuilder();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        byte[] decryptedSignature = cipher.doFinal(signature);
        for (byte b : decryptedSignature) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Decrypted signature hex: {}", sb.toString());


        if (!Arrays.equals(qinfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - qinfoDigest.length, decryptedSignature.length))) {
            throw new IOException("Quote verification failed");
        }

        for (pcr = 0; pcr < 8 * selectLength; pcr++) {
            pcrValues = pcrValues + String.format("%2d", pcr) + " ";
            while (pcrIndex < pcrLength) {
                pcrValues = pcrValues + String.format("%02x", pcrs[pcrIndex]);
                pcrIndex++;
                if ((pcrIndex) % 20 == 0)
                    break;
            }
            pcrValues = pcrValues + "\n";
        }
        log.debug("aikqverify output:\n {}", pcrValues);
        return pcrValues;
    }
}