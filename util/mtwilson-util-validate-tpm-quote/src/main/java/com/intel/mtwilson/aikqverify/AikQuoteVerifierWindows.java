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
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class AikQuoteVerifierWindows {

    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifierWindows.class);

    public String verifyAIKQuoteWindows(byte[] challenge, byte[] quoteFileContent, PublicKey rsaPublicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {


        byte[] quote = null;
        byte[] pcrs = null;
        byte[] signature = null;
        byte[] chalmd = new byte[20];
        byte[] qinfoDigest;
        byte[] quoteNonce;
        byte[] pcrSelect;
        byte[] compositeHash = new byte[20];
        byte[] pcrConcat = new byte[20 * 24];
        byte[] pcrComposite = new byte[20 * 24 + 9];
        StringBuilder sb = new StringBuilder();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int selectLength = 0;
        int pcrLength = 0;
        int pcr;
        int pcrBytesIndex = 0;
        String pcrValues = "";


        messageDigest.update(challenge);
        chalmd = messageDigest.digest();
        messageDigest.reset();
        for (byte b : chalmd) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Challenge message digest hex: {}", sb.toString());
        sb = new StringBuilder();


        int headerLength = ByteBuffer.wrap(quoteFileContent, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Header length: {}", headerLength);
        pcrLength = ByteBuffer.wrap(quoteFileContent, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("PCR length: {}", pcrLength);
        int quoteLength = ByteBuffer.wrap(quoteFileContent, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Quote length extracted: {}", quoteLength);
        int signatureLength = ByteBuffer.wrap(quoteFileContent, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        log.debug("Signature length: {}", signatureLength);
        pcrs = Arrays.copyOfRange(quoteFileContent, headerLength, headerLength + pcrLength);
        for (byte b : pcrs) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR hex: {}", sb.toString());
        sb = new StringBuilder();

        quote = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength, headerLength + pcrLength + quoteLength);
        for (byte b : quote) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Quote hex: {}", sb.toString());
        sb = new StringBuilder();

        quoteNonce = Arrays.copyOfRange(quote, 6, 26);
        for (byte b : quoteNonce) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Quote nonce: {}", sb.toString());
        sb = new StringBuilder();

        if (!Arrays.equals(chalmd, quoteNonce)) {
            log.error("Nonce in quote does not match nonce in challenge file");
            throw new IOException("Nonce in quote does not match nonce in challenge file");
        }
        qinfoDigest = messageDigest.digest(quote);
        for (byte b : qinfoDigest) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Quote digest hex: {}", sb.toString());
        sb = new StringBuilder();
        signature = Arrays.copyOfRange(quoteFileContent, headerLength + pcrLength + quoteLength, headerLength + pcrLength + quoteLength + signatureLength);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        byte[] decryptedSignature = cipher.doFinal(signature);

        for (byte b : decryptedSignature) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Decrypted signature hex: {}", sb.toString());
        sb = new StringBuilder();

        if (!Arrays.equals(qinfoDigest, Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - qinfoDigest.length, decryptedSignature.length))) {
            log.error("Quote verification failed");
            throw new IOException("Quote verification failed");
        }

        selectLength = ByteBuffer.wrap(quote, 6 + 20, 2).getShort();
        log.debug("Select length: {}", selectLength);
        pcrSelect = Arrays.copyOfRange(quote, 6 + 20 + 2, 6 + 20 + 2 + selectLength);
        for (byte b : pcrSelect) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR select: {}", pcrSelect);
        log.debug("PCR select hex: {}", sb.toString());
        sb = new StringBuilder();
        compositeHash = Arrays.copyOfRange(quote, 6 + 20 + 2 + selectLength + 1, 6 + 20 + 2 + selectLength + 1 + 20);
        for (byte b : compositeHash) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("Composite hex: {}", sb.toString());
        sb = new StringBuilder();
        int concatLength = 0;
        int numOfPCRNotIncluded = 0;
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
        for (byte b : pcrConcat) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR concat hex: {}", sb.toString());
        sb = new StringBuilder();

        log.debug("Concat length: {}", concatLength);
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(concatLength);
        baos.write(Arrays.copyOfRange(quote, 26, 26 + 2 + selectLength));
        baos.write(bb.array());
        baos.write(pcrConcat);
        pcrComposite = baos.toByteArray();

        for (byte b : pcrComposite) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR composite hex: {}", sb.toString());
        sb = new StringBuilder();

        byte[] pcrDigest = messageDigest.digest(Arrays.copyOfRange(pcrComposite, 0, 2 + selectLength + 4 + concatLength));
        for (byte b : pcrDigest) {
            sb.append(String.format(" %02x ", b));
        }
        log.debug("PCR digest hex: {}", sb.toString());
        sb = new StringBuilder();

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
