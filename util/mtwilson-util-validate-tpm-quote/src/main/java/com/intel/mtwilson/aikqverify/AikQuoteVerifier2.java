/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.aikqverify;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang.ArrayUtils;


/**
 * AikQuoteVerifier class can be used to validate tpm quote generated from both linux and windows platforms
 * and also return pcr values.
 *
 * @author rpravee1
 */

public class AikQuoteVerifier2 {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifier2.class);
    private static final int SHA1_SIZE = 20;
    private static final int SHA256_SIZE = 32;
    //Hash Algorithm ID for SHA1, SHA256, SM3 SHA256 according to TCG standards are 0x04,0x0B and 0x012 respectively
    private static final int TPM_API_ALG_ID_SHA256 = 0x0B;
    private static final int TPM_API_ALG_ID_SHA1 = 0x04;
    private static final int TPM_API_ALG_ID_SM3_SHA256 = 0x012;
    private static final int MAX_PCR_BANKS = 3;

    /**
     * verifyAIKQuote Method takes arguments challenge, quote as byte arrays and RSA Public key and returns the list of PCR
     * values as string.
     */
    public String verifyAIKQuote(byte[] challenge, byte[] quote, PublicKey rsaPublicKey) {

        int index = 0;
        int quoteInfoLen = ByteBuffer.wrap(quote,0, 2).getShort();

        index += 2;
        byte [] quoteInfo = Arrays.copyOfRange(quote, index, index + quoteInfoLen);
        //qualifiedSigner -- skip the magic header and type -- not interested
        index += 6;

        int tpm2bNameSize = ByteBuffer.wrap(quote, index, 2).getShort();
        index += 2;

        //skip the tpm2bName -- not interested
        index += tpm2bNameSize;
        int tpm2bDataSize = ByteBuffer.wrap(quote, index, 2).getShort();

        index += 2;
        byte []tpm2bData = Arrays.copyOfRange(quote, index, index + tpm2bDataSize);
        byte []recvNonce = tpm2bData;

        if (!Arrays.equals(recvNonce, challenge)){
            throw new IllegalStateException("AIK Quote verification failed, Nonce received does not matches with challenge");
        }
        log.info("Successfully verified challenge response");
        index = index + tpm2bDataSize;
        /* Parse quote file
         * The quote result is constructed as follows for now
         *
         * part1: pcr values (0-23), sha1 pcr bank. so the length is 20*24=480
         * part2: the quoted information: TPM2B_ATTEST
         * part3: the signature: TPMT_SIGNATURE
         */
        index += 17; // skip over the TPMS_CLOCKINFO structure - Not interested
        index += 8;  // skip over the firmware info - Not interested
        int pcrBankCount = ByteBuffer.wrap(quote, index, 4).getInt();

        log.debug("no of pcr banks: {}", pcrBankCount);
        if (pcrBankCount > MAX_PCR_BANKS){
            throw new IllegalStateException("AIK Quote verification failed, Number of PCR selection array in " +
                    "the quote is greater 3, pcrBankCount" + pcrBankCount);
        }

        index += 4;

        PcrSelection[] pcrSelection = new PcrSelection[pcrBankCount];
        for (int i=0; i<pcrBankCount; i++) {
            pcrSelection[i] = new PcrSelection();
            pcrSelection[i].setHashAlg(ByteBuffer.wrap(quote, index, 2).getShort());
            index += 2;
            pcrSelection[i].setSize(ByteBuffer.wrap(quote, index, 1).get());
            index += 1;
            pcrSelection[i].setPcrSelected(Arrays.copyOfRange(quote, index, index + pcrSelection[i].size));
            index += pcrSelection[i].getSize();
        }

        int tpm2bDigestSize = ByteBuffer.wrap(quote, index, 2).getShort();
        index += 2;
        byte [] tpm2bDigest = Arrays.copyOfRange(quote, index, index + tpm2bDigestSize);

        /* PART 2: TPMT_SIGNATURE
        Skip the first 2 bytes having the quote info size and remaining bytes, which includes signer info, nonce, pcr selection
        and extra data. So jump to TPMT_SIGNATURE
        */

        int tpmtSigIndex = 2 + quoteInfoLen;
        byte[] tpmtSig = Arrays.copyOfRange(quote, tpmtSigIndex, quote.length);
        int pos = 0;
        /* sigAlg -indicates the signature algorithm
         * TPMI_SIG_ALG_SCHEME
         * for now, it is TPM_ALG_RSASSA with value 0x0014
         */
        int tpmtSignatureAlg = ByteBuffer.wrap(tpmtSig, 0, 2).getShort();
        /* hashAlg used by the signature algorithm indicated above
         * TPM_ALG_HASH
         * for TPM_ALG_RSASSA, the default hash algorithm is TPM_ALG_SHA256 with value 0x000b
         */
        log.debug("tpm signature Algorithm: {}", tpmtSignatureAlg);
        pos += 2;
        int tpmtSignatureHashAlg = ByteBuffer.wrap(tpmtSig, pos, 2).getShort();
        log.debug("tpm signature Hash Algorithm: {}", tpmtSignatureHashAlg);

        pos += 2;
        int tpmtSignatureSize = ByteBuffer.wrap(tpmtSig, pos, 2).getShort();
        log.debug("tpm signature size: {}", tpmtSignatureSize);

        pos += 2;
        byte [] tpmtSignature = Arrays.copyOfRange(tpmtSig,  pos, pos + tpmtSignatureSize);

        Cipher cipher = null;
        byte[] decryptedSignature = new byte[0];
        MessageDigest digest = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            decryptedSignature = cipher.doFinal(tpmtSignature);
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new IllegalStateException("AIK Quote verification failed, Failed to decrypt the signature", ex);
        }

        log.info("RSA verification of tpm signature is successfully done");

        pos = pos + tpmtSignatureSize;
        int pcrLen = quote.length - (pos + tpmtSigIndex);
        log.debug("pcrLen: {}", pcrLen);
        if (pcrLen <=0) {
            throw new IllegalStateException("AIK Quote verification failed, No PCR values included in quote");
        }

        byte [] pcrs = Arrays.copyOfRange(tpmtSig, pos, pos + pcrLen);

        final byte[] quoteDigest = digest.digest(quoteInfo);
        
        /* Decrypted Signature has Header 19 bytes along with the signature,
         So signature starts from position (decryptedSignature.length - quoteDigest.length)*/
        byte[] tpmt_signature = Arrays.copyOfRange(decryptedSignature, decryptedSignature.length - quoteDigest.length,
                decryptedSignature.length);
        if (!Arrays.equals(tpmt_signature, quoteDigest)){
            throw new IllegalStateException("AIK Quote verification failed, Decrypted signature does not match with tpm quote digest");
        }

        // validate the PCR concatenated digest
        int pcri=0, ind=0, concatSize=0, pcrPos=0;
        int hashAlg = 0;
        int pcrSize = 0;
        byte[] pcrConcat = null;
        //pcrConcatLen is size of bytes having all the pcr values in the quote. i.e, size of SHA256 in bytes * no of pcr entries=24 * pcr banks=3
        int pcrConcatLen = SHA256_SIZE * 24 * 3;
        StringBuilder sb = new StringBuilder();

        for (int j=0; j<pcrBankCount; j++) {
            hashAlg = pcrSelection[j].getHashAlg();
            if (hashAlg == TPM_API_ALG_ID_SHA1)
                pcrSize = SHA1_SIZE;
            else if (hashAlg == TPM_API_ALG_ID_SHA256 || hashAlg == TPM_API_ALG_ID_SM3_SHA256)
                pcrSize = SHA256_SIZE;
            else {
                throw new IllegalStateException("AIK Quote verification failed, Unsupported PCR banks, hash algorithm id: " + hashAlg);
            }
            /* For each pcr bank iterate through each pcr selection array.
               Here pcrSelection.pcrSelected byte array contains 3 elements, where each bit of this element corresponds to pcr entry.
               8 bits pcrSelection.pcrSelected value corresponds to 8 PCR entries.
            */
            for (int pcr=0; pcr < 8*pcrSelection[j].getSize(); pcr++) {
                byte [] pcrSelected = pcrSelection[j].getPcrSelected();
                int selected = pcrSelected[pcr/8] & (1 << (pcr%8));
                if ( selected > 0 ) {
                    if ((pcrPos + pcrSize) < pcrConcatLen) {
                        pcrConcat = concatenate(pcrConcat, Arrays.copyOfRange(pcrs, pcrPos, pcrPos + pcrSize));
                    }
                    if (hashAlg == TPM_API_ALG_ID_SHA1)
                        sb.append(String.format("%2d ", pcr));
                    else if (hashAlg == TPM_API_ALG_ID_SHA256)
                        sb.append(String.format("%2d_SHA256 ", pcr));
		    //Ignore the pcr banks other than SHA1 and SHA256
                    if(hashAlg == TPM_API_ALG_ID_SHA1 || hashAlg == TPM_API_ALG_ID_SHA256) {
                        for (int i = 0; i < pcrSize; i++) {
                            sb.append(String.format("%02x", pcrs[pcrPos + i]));
                        }
                    }
                    sb.append("\n");
                    pcri++;
                    ind++;
                    concatSize += pcrSize;
                    pcrPos += pcrSize;
                }
            }
        }

        if (ind<1) {
            throw new IllegalStateException("AIK Quote verification failed, None of the PCRs selected for the quote");
        }

        final byte[] pcrDigest = digest.digest(pcrConcat);
        if(!Arrays.equals(pcrDigest, tpm2bDigest)){
            throw new IllegalStateException("AIK Quote verification failed, Digest of Concatenated PCR values " +
                    "does not match with PCR digest in the quote");
        }

        log.info("Successfully verified AIK Quote");
        return sb.toString();
    }

    //Helper Function for concatenation of pcr values
    public static <T> byte[] concatenate(byte[] a, byte[] b)
    {
        byte[] c = (byte[]) ArrayUtils.addAll(a, b);
        return c;
    }

    //Helper class for storing pcr selections
    public class PcrSelection{
        int size;
        int hashAlg;
        byte[] pcrSelected;

        void setSize (int size){
            this.size = size;
        }
        void setHashAlg (int hashAlg){
            this.hashAlg = hashAlg;
        }
        void setPcrSelected (byte [] pcrSelected){
            this.pcrSelected = pcrSelected;
        }

        int getSize(){
            return this.size;
        }
        int getHashAlg(){
            return this.hashAlg;
        }
        byte [] getPcrSelected(){
            return this.pcrSelected;
        }
    }

}
