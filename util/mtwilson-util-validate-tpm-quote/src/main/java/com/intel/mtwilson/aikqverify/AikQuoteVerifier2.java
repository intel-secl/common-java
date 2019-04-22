/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

package com.intel.mtwilson.aikqverify;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang.ArrayUtils;


/**
 *
 * @author rpravee1
 */

public class AikQuoteVerifier2 {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AikQuoteVerifier2.class);
    private static final int SHA1_SIZE = 20;
    private static final int SHA256_SIZE = 32;
    private static final int TPM_API_ALG_ID_SHA256 = 11;
    private static final int TPM_API_ALG_ID_SHA1 = 4;
    private static final int MAX_BANKS = 3;

    public String getAikverifyLinux(byte[] challenge, byte[] quoteBytes, PublicKey rsaPublicKey) {

        int index = 0;
        int quotedInfoLen = ByteBuffer.wrap(quoteBytes,0, 2).getShort();

        index += 2;
        byte [] quotedInfo = Arrays.copyOfRange(quoteBytes, index, index + quotedInfoLen);
        index += 6;

        int tpm2bNameSize = ByteBuffer.wrap(quoteBytes, index, 2).getShort();
        index += 2;
        index += tpm2bNameSize;
        int tpm2bDataSize = ByteBuffer.wrap(quoteBytes, index, 2).getShort();

        index += 2;
        byte []tpm2bData = Arrays.copyOfRange(quoteBytes, index, index + tpm2bDataSize);
        byte []recvNonce = tpm2bData;

        if (!Arrays.equals(recvNonce, challenge)){
            log.debug("error matching nonce with challenge");
            return null;
        }
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
        int pcrBankCount = ByteBuffer.wrap(quoteBytes, index, 4).getInt();

        log.debug("no of pcr banks: {}", pcrBankCount);
        if (pcrBankCount > 3){
            log.error("number of PCR selection array in the quote is greater 3 {}", pcrBankCount);
            return "";
        }

        index += 4;

        PcrSelection[] pcrSelection = new PcrSelection[pcrBankCount];
        for (int i=0; i<pcrBankCount; i++) {
            pcrSelection[i] = new PcrSelection();
            pcrSelection[i].setHashAlg(ByteBuffer.wrap(quoteBytes, index, 2).getShort());
            index += 2;
            pcrSelection[i].setSize(ByteBuffer.wrap(quoteBytes, index, 1).get());
            index += 1;
            pcrSelection[i].setPcrSelected(Arrays.copyOfRange(quoteBytes, index, index + pcrSelection[i].size));
            index += pcrSelection[i].getSize();
        }

        int tpm2bDigestSize = ByteBuffer.wrap(quoteBytes, index, 2).getShort();
        index += 2;
        byte [] tpm2bDigest = Arrays.copyOfRange(quoteBytes, index, index + tpm2bDigestSize);

        /* PART 2: TPMT_SIGNATURE */
        int tpmtSigIndex = 2 + quotedInfoLen; // jump to the TPMT_SIGNATURE strucuture
        byte[] tpmtSig = Arrays.copyOfRange(quoteBytes, tpmtSigIndex, quoteBytes.length);
        int pos = 0;
        /* sigAlg -indicates the signature algorithm
         * TPMI_SIG_ALG_SCHEME
         * for now, it is TPM_ALG_RSASSA with value 0x0014
         */
        int tpmtSignatureAlg = ByteBuffer.wrap(tpmtSig, 0, 2).getShort(); // This is NOT in networ order
        /* hashAlg used by the signature algorithm indicated above
         * TPM_ALG_HASH
         * for TPM_ALG_RSASSA, the default hash algorihtm is TPM_ALG_SHA256 with value 0x000b
         */
        log.debug("tpmt signature Algorithm: {}", tpmtSignatureAlg);
        pos += 2;
        int tpmtSignatureHashAlg = ByteBuffer.wrap(tpmtSig, pos, 2).getShort(); // This is NOT in network order
        log.debug("tpmt signature Hash Algorithm: {}", tpmtSignatureHashAlg);

        pos += 2;
        int tpmtSignatureSize = ByteBuffer.wrap(tpmtSig, pos, 2).getShort(); // This is NOT in network order
        log.debug("tpmt signature size: {}", tpmtSignatureSize);

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
            log.error("could not decrypt signature: {}", ex.getMessage());
            return "";
        }

        int numPcrs = tpmtSigIndex + pos + tpmtSignatureSize;
        int pcrLen = quoteBytes.length - (numPcrs - index);
        log.debug("pcrLen: {}", pcrLen);
        if (pcrLen <=0) {
            log.error("no PCR values included in quote\n");
            return "";
        }
        byte [] pcrs = Arrays.copyOfRange(tpmtSig, pos + tpmtSignatureSize, tpmtSig.length);

        final byte[] hashbytes = digest.digest(quotedInfo);

        byte[] tpmt_signature = Arrays.copyOfRange(decryptedSignature, 19, decryptedSignature.length);

        if (!Arrays.equals(tpmt_signature, hashbytes)){
            log.error("rsa verification failed");
            return "";
        }

        // validate the PCR concatenated digest
        int pcri=0, ind=0, concatSize=0, pcrPos=0;
        int hashAlg = 0;
        int pcrSize = 0;
        byte[] pcrConcat = null;
        //List<Byte []> pcrConcat = new ArrayList<Byte []>();
        int pcrConcatLen = SHA256_SIZE * 24 * 3;
        StringBuilder sb = new StringBuilder();
        for (int j=0; j<pcrBankCount; j++) {
            hashAlg = pcrSelection[j].getHashAlg();
            if (hashAlg == 0x04)
                pcrSize = SHA1_SIZE;
            else if (hashAlg == 0x0B)
                pcrSize = SHA256_SIZE;
            else {
                log.error("Not supported PCR banks {} in quote\n", hashAlg);
                return "";
                //returnCode = 3;
            }

            for (int pcr=0; pcr < 8*pcrSelection[j].getSize(); pcr++) {
                byte [] pcrSelected = pcrSelection[j].getPcrSelected();

                int selected = pcrSelected[pcr/8] & (1 << (pcr%8));
                if ( selected > 0 ) {

                    if ((pcrPos + pcrSize) < pcrConcatLen) {
                        pcrConcat = concatenate(pcrConcat, Arrays.copyOfRange(pcrs, pcrPos, pcrPos + pcrSize));
                    }
                    if (hashAlg == 0x04)
                        sb.append(String.format("%2d ", pcr));
                    else if (hashAlg == 0x0B)
                        sb.append(String.format("%2d_SHA256 ", pcr));
                    for (int i=0; i<pcrSize; i++) {
                        sb.append(String.format("%02x", pcrs[pcrPos+i]));
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
            log.error("Error, no PCRs selected for quote\n");
            return "";
        }

        final byte[] pcrDigest = digest.digest(pcrConcat);

        if(!Arrays.equals(pcrDigest, tpm2bDigest)){
            log.error("Error in comparing the concatenated PCR digest with the digest in quote");
            return "";
        }

        log.info("qoute validation successfully done");
        return sb.toString();
    }

    public static <T> byte[] concatenate(byte[] a, byte[] b)
    {
        byte[] c = (byte[]) ArrayUtils.addAll(a, b);
        return c;
    }

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

    public Map<Integer, Map<String, String>>  getAikverifyWin(byte[] chalBytes, byte[] quoteBytes, PublicKey rsaPublicKey) {
        int pcrAlgId = TPM_API_ALG_ID_SHA1;
        int digestSize = SHA1_SIZE;
        int cbQoute = 0;
        int cbSignature = 0;
        int cbLog = 0;
        int index = 0;
        HashMap<String, String> pcrsMap = new HashMap<String, String>();
        Map<Integer, Map<String, String>> pcrsAlgMap  = new LinkedHashMap<>();

        /*
        typedef struct _PCP_PLATFORM_ATTESTATION_BLOB2 {
            UINT32 Magic;
            UINT32 Platform;
            UINT32 HeaderSize;
            UINT32 cbPcrValues;
            UINT32 cbQuote;
            UINT32 cbSignature;
            UINT32 cbLog;
            UINT32 PcrAlgorithmId;
        } PCP_PLATFORM_ATTESTATION_BLOB2, *PPCP_PLATFORM_ATTESTATION_BLOB2;

        typedef struct _PCP_PLATFORM_ATTESTATION_BLOB {
          UINT32 Magic;
          UINT32 Platform;
          UINT32 HeaderSize;
          UINT32 cbPcrValues;
          UINT32 cbQuote;
          UINT32 cbSignature;
          UINT32 cbLog;
        } PCP_PLATFORM_ATTESTATION_BLOB, *PPCP_PLATFORM_ATTESTATION_BLOB;
         */

        MessageDigest digest = null;
        Cipher cipher = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        } catch (NoSuchAlgorithmException ex) {
            log.error("Could not initialize message digest for SHA - 1 Algorithm : {}", ex.getMessage());
        }
        catch (InvalidKeyException | NoSuchPaddingException ex) {
            log.error("Could not initialize cipher with RSA Public Key");
        }

        final byte[] chalmd = digest.digest(chalBytes);

        byte[] pAttestation = Arrays.copyOfRange(quoteBytes, 0, 28);
        byte[] pAttestation2 = Arrays.copyOfRange(quoteBytes, 0, 32);

        int cursor = ByteBuffer.wrap(pAttestation, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int tpmVersion = ByteBuffer.wrap(pAttestation, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        pcrAlgId = ByteBuffer.wrap(pAttestation2, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int cbPcrValues = ByteBuffer.wrap(pAttestation, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getShort();

        if (pcrAlgId == TPM_API_ALG_ID_SHA256) {
            digestSize = SHA256_SIZE;
        }

        byte[] pbPcrValues =  Arrays.copyOfRange(quoteBytes, cursor, cursor + cbPcrValues);

        log.debug("pcrAlgId: {}", pcrAlgId);
        log.debug("tpmVersion {}", tpmVersion);

        cursor += cbPcrValues;
        byte[] pbQuote = null;
        byte[] pbSignature = null;
        byte[] pbLog = null;
        cbQoute = ByteBuffer.wrap(pAttestation, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getShort();

        if(cbQoute != 0){
            pbQuote = Arrays.copyOfRange(quoteBytes, cursor, cursor + cbQoute);
            cursor += cbQoute;
        }

        cbSignature = ByteBuffer.wrap(pAttestation, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (cbSignature != 0){
            pbSignature = Arrays.copyOfRange(quoteBytes, cursor, cursor + cbSignature);
            cursor += cbSignature;
        }

        cbLog = ByteBuffer.wrap(pAttestation, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if(cbLog != 0){
            pbLog = Arrays.copyOfRange(quoteBytes, cursor, cursor + cbLog);
            cursor += cbLog;
        }

        final byte[] quoteDigest = digest.digest(pbQuote);

        byte[] decryptedSignature = new byte[0];
        try {
            decryptedSignature = cipher.doFinal(pbSignature);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            log.error("Could not decrypt signature: {}", ex.getMessage());
        }
        byte[] tpmt_signature = Arrays.copyOfRange(decryptedSignature, 15, decryptedSignature.length);

        if (!Arrays.equals(tpmt_signature, quoteDigest)){
            log.error("rsa verification failed");
        }

        byte[] tpmQuoted = new byte[0];
        int tpm2bNameSize = 0;
        int tpm2bDataSize = 0;
        int recvNonceLen = 0;
        byte[] tpm2bNameBuffer;
        byte[] tpm2bDataBuffer;
        byte[] recvNonce;
        // validate nonce
        if (tpmVersion==2) {
            tpmQuoted = Arrays.copyOfRange(pbQuote, index, pbQuote.length);
            //qualifiedSigner -- skip the magic header and type -- not interested
            index += 6;
            tpm2bNameSize = ByteBuffer.wrap(tpmQuoted, index, 2).getShort();
            index += 2;
            tpm2bNameBuffer = Arrays.copyOfRange(tpmQuoted, index, index + tpm2bNameSize);

            index += tpm2bNameSize;
            tpm2bDataSize = ByteBuffer.wrap(tpmQuoted, index, 2).getShort();
            recvNonceLen = tpm2bDataSize;
            index += 2;
            tpm2bDataBuffer = Arrays.copyOfRange(tpmQuoted, index, index + tpm2bDataSize);
            recvNonce = tpm2bDataBuffer;
            index += tpm2bDataSize;
            log.debug("chalmd :{}", chalmd);
            log.debug("recvNonce :{}", recvNonce);
            if(!Arrays.equals(chalmd, recvNonce)){
                log.error("Error in comparing the received nonce with the challenge");
            }
        }

        // prepare to verify PCR digest
        index += 17; // skip over the TPMS_CLOCKINFO structure - Not interested
        index += 8;  // skip over the firmware info - Not interested
        /* TPMU_ATTEST with selected PCR banks and PCRs, and their hash
         * tpms_quote_info tpml_pcr_selection
         *	count 			uint32	0x00000001	 4 bytes -indicates the number of tpms_pcr_slection array
         *	tpms_pcr_selection	hash algorithm	uint16	2 bytes
         *				size of bit map	uint8	1 byte
         *				pcrSelect		size of bytes
         *	tpms_pcr_selection
         *	...
         *	tpm2b_digest		size	0x0020	2 bytes
         *				digest	32 bytes of hash
         */

        int pcrBankCount = ByteBuffer.wrap(tpmQuoted, index, 4).getInt();
        log.debug("pcrBankCount :{}", pcrBankCount);

        index += 4;
        if (pcrBankCount > MAX_BANKS) {
            log.error("number of PCR selection array in the quote is greater than: {}", MAX_BANKS);
        }

        // processing the tpms_pcr_selection array
        PcrSelection[] pcrSelection = new PcrSelection[pcrBankCount];
        for (int i = 0; i < pcrBankCount; i++) {
            pcrSelection[i] = new PcrSelection();
            pcrSelection[i].setHashAlg(ByteBuffer.wrap(tpmQuoted, index, 2).getShort());
            index += 2;
            pcrSelection[i].setSize(ByteBuffer.wrap(tpmQuoted, index, 1).get());
            index += 1;
            pcrSelection[i].setPcrSelected(Arrays.copyOfRange(tpmQuoted, index, index + pcrSelection[i].size));
            index += pcrSelection[i].getSize();
        }

        int tpm2bDigestSize = ByteBuffer.wrap(tpmQuoted, index, 2).getShort();
        index += 2;
        byte[] tpm2bDigest = Arrays.copyOfRange(tpmQuoted, index, index + tpm2bDigestSize);

        // validate the PCR concatenated digest
        int pcri = 0, ind = 0, concatSize = 0, pcrPos = 0;
        int hashAlg = 0;
        int pcrSize = 0;
        byte[] pcrConcat = null;
        int pcrConcatLen = SHA256_SIZE * 24 * 3;
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < pcrBankCount; j++) {
            hashAlg = pcrSelection[j].getHashAlg();
            if (hashAlg == 0x04)
                pcrSize = SHA1_SIZE;
            else if (hashAlg == 0x0B)
                pcrSize = SHA256_SIZE;
            else {
                log.error("Not supported PCR banks {} in quote\n", hashAlg);
            }

            for (int pcr = 0; pcr < 8 * pcrSelection[j].getSize(); pcr++) {
                byte[] pcrSelected = pcrSelection[j].getPcrSelected();

                int selected = pcrSelected[pcr / 8] & (1 << (pcr % 8));
                if (selected > 0) {

                    if ((pcrPos + pcrSize) < pcrConcatLen) {
                        pcrConcat = concatenate(pcrConcat, Arrays.copyOfRange(pbPcrValues, pcr * pcrSize, pcr * pcrSize + pcrSize));
                    }
                    for (int i = 0; i < pcrSize; i++) {
                        sb.append(String.format("%02x", pbPcrValues[pcrPos + i]));
                    }
                    pcrsMap.put(String.valueOf(pcr), sb.toString());
                    sb.setLength(0);
                    pcri++;
                    ind++;
                    concatSize += pcrSize;
                    pcrPos += pcrSize;
                }
            }
            pcrsAlgMap.put(pcrSize, pcrsMap);
        }
        if (ind < 1) {
            log.error("Error, no PCRs selected for quote\n");
        }

        final byte[] pcrDigest = digest.digest(pcrConcat);

        if (!Arrays.equals(pcrDigest, tpm2bDigest)) {
            log.error("Error in comparing the concatenated PCR digest with the digest in quote");
        }

        log.info("qoute validation successfully done");
        return pcrsAlgMap;
    }

}
