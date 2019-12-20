/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.tpm20;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.util.tpm20.x509.TpmCertifyKeyInfo;
import com.intel.mtwilson.util.tpm20.x509.TpmCertifyKeySignature;
import gov.niarl.his.privacyca.TpmCertifyKey20;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author ssbangal and jbuhacoff
 */
public class CertifyKey20 {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyKey20.class);
    // This OID is used for storing the TCG standard certificate as an attr within the x.509 cert.
    // We are using this OID as we could not find any specific OID for the certifyKey structure.
    public static final String TCG_STRUCTURE_CERTIFY_INFO_OID = "2.5.4.133.3.2.41";
    public static final String TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID = "2.5.4.133.3.2.41.1";
    public static final String TCG_STRUCTURE_CERTIFY_INFO_ENC_SCHEME_OID = "2.5.4.133.3.2.41.2";


    public static boolean verifyTpmBindingKeyCertificate(X509Certificate keyCertificate, PublicKey aikPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, TpmUtils.TpmBytestreamResouceException, TpmUtils.TpmUnsignedConversionException {
        /*
         * First check that the given AIK public key can verify the signature
         * on the TPM binding public key.
         */
        TpmCertifyKeyInfo tpmCertifyKeyInfo;
        TpmCertifyKeySignature tpmCertifyKeySignature;

        try {
            tpmCertifyKeyInfo = TpmCertifyKeyInfo.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeyInfo.OID));
            tpmCertifyKeySignature = TpmCertifyKeySignature.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeySignature.OID));
        } catch (IOException e) {
            log.debug("Cannot parse X509 extensions TpmCertifyKeyInfo and TpmCertifyKeySignature", e);
            return false;
        }

        try {
            if (!isCertifiedKeySignatureValid(tpmCertifyKeyInfo.getBytes(), tpmCertifyKeySignature.getBytes(), aikPublicKey)) {
                log.debug("TPM Binding Public Key cannot be verified by the given AIK public key");
                return false;
            }
        } catch (GeneralSecurityException | DecoderException e) {
            log.debug("Cannot verify TPM Binding Public Key signature", e);
            return false;
        }

        /*
         * Second, check that the certified key information indicates a binding key
         */
        try {
            if (!isBindingKey(new TpmCertifyKey20(tpmCertifyKeyInfo.getBytes()))) {
                log.debug("TPM Binding Key has incorrect attributes");
                return false;
            }
        } catch (TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException e) {
            log.debug("Cannot verify TPM Binding Public Key attributes", e);
            return false;
        }
        return true;

    }

    public static boolean verifyTpmSigningKeyCertificate(X509Certificate keyCertificate, PublicKey aikPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, TpmUtils.TpmBytestreamResouceException, TpmUtils.TpmUnsignedConversionException {
        /*
         * First check that the given AIK public key can verify the signature
         * on the TPM binding public key.
         */
        TpmCertifyKeyInfo tpmCertifyKeyInfo;
        TpmCertifyKeySignature tpmCertifyKeySignature;

        try {
            tpmCertifyKeyInfo = TpmCertifyKeyInfo.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeyInfo.OID));
            tpmCertifyKeySignature = TpmCertifyKeySignature.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeySignature.OID));
        } catch (IOException e) {
            log.debug("Cannot parse X509 extensions TpmCertifyKeyInfo and TpmCertifyKeySignature", e);
            return false;
        }

        try {
            if (!isCertifiedKeySignatureValid(tpmCertifyKeyInfo.getBytes(), tpmCertifyKeySignature.getBytes(), aikPublicKey)) {
                log.debug("TPM Binding Signing Key cannot be verified by the given AIK public key");
                return false;
            }
        } catch (GeneralSecurityException | DecoderException e) {
            log.debug("Cannot verify TPM Signing Public Key signature", e);
            return false;
        }

        /*
         * Second, check that the certified key information indicates a binding key
         */
        try {
            if (!isSigningKey(new TpmCertifyKey20(tpmCertifyKeyInfo.getBytes()))) {
                log.debug("TPM Signing Key has incorrect attributes");
                return false;
            }
        } catch (TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException e) {
            log.debug("Cannot verify TPM Signing Public Key attributes", e);
            return false;
        }
        return true;
    }

    /**
     * If you have an X.509 key certificate signed by Mt Wilson for a TPM
     * binding key or signing key, use the {@code verifyTpmKeyCertificate()}
     * function instead.
     *
     * This function validates the certify key against the specified signature
     * using the AIK certificate that was used during the key certification.
     *
     * @param certifyKeyDataBlob
     * @param certifyKeySignatureBlob
     * @param aikPublicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static boolean isCertifiedKeySignatureValid(byte[] certifyKeyDataBlob, byte[] certifyKeySignatureBlob, PublicKey aikPublicKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, DecoderException, TpmUtils.TpmBytestreamResouceException, TpmUtils.TpmUnsignedConversionException {
        //TPM2.0 has 4 additional bytes vs. TPM 1.2
        byte[] oidPadding = Hex.decodeHex("3031300d060960864801650304020105000420".toCharArray());
        short SHA256_ENCRYPTION_SCHEME = 0x000b;
        try {

            log.debug("Verifying the certify key signature against the AIK cert which signed it.");
            //Check encryption scheme used, Linux TPM 2.0 default is sha256, on Windows Tpm2.0 Sha1 is default
            TpmCertifyKey20 tpmCertifyKey20 = new TpmCertifyKey20(certifyKeyDataBlob);
            short hashAlg = tpmCertifyKey20.getTpmuAttest().getTpmsCertifyInfoBlob().getTpmtHa().getHashAlg();
            log.debug("Checking encryption sheme {} vs {}, result is {}",SHA256_ENCRYPTION_SCHEME,hashAlg,SHA256_ENCRYPTION_SCHEME == hashAlg);
            
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, aikPublicKey);
            byte[] signedDigest;
            if (certifyKeySignatureBlob.length > 256) {
                log.debug("Length of certifyKeySignatureBlob is larger then 256, TPM 2.0. Will only parse out the required 256 bytes: {}", Hex.encodeHexString(Arrays.copyOfRange(certifyKeySignatureBlob, certifyKeySignatureBlob.length - 256, certifyKeySignatureBlob.length)));
                signedDigest = cipher.doFinal(Arrays.copyOfRange(certifyKeySignatureBlob, certifyKeySignatureBlob.length - 256, certifyKeySignatureBlob.length));
            } else {
                log.debug("Length of certifyKeySignatureBlob is 256 or less, TPM 1.2.");
                signedDigest = cipher.doFinal(certifyKeySignatureBlob);
            }
            byte[] signedDigestWithoutOidPadding = Arrays.copyOfRange(signedDigest, oidPadding.length, signedDigest.length);
            byte[] computedDigest;
            if(SHA256_ENCRYPTION_SCHEME == hashAlg)
                computedDigest = Sha256Digest.digestOf(certifyKeyDataBlob).toByteArray();
            else
                computedDigest = Sha1Digest.digestOf(certifyKeyDataBlob).toByteArray();
            log.debug("Verifying the signed digest {} of size {} against the computed digest {} of size {}",
                    Hex.encodeHexString(signedDigestWithoutOidPadding), signedDigestWithoutOidPadding.length, //TpmUtils.byteArrayToHexString(signedDigestWithoutOidPadding), 
                    Hex.encodeHexString(computedDigest), computedDigest.length); //TpmUtils.byteArrayToHexString(computedDigest));

            boolean result = Arrays.equals(signedDigestWithoutOidPadding, computedDigest);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }

    }

    /**
     * If you have an X.509 key certificate signed by Mt Wilson for a TPM binding key
     * or signing key, use the {@code verifyTpmKeyCertificate()} function instead.
     *
     * This function validates the certify key against the specified signature using the AIK certificate that was used during the key certification.
     * @param certifyKeyDataBlob
     * @param certifyKeySignatureBlob
     * @param aikPublicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static boolean isCertifiedKeySignatureValidWin(byte[] certifyKeyDataBlob, byte[] certifyKeySignatureBlob, PublicKey aikPublicKey)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, DecoderException {
        byte[] oidPadding = Hex.decodeHex("3021300906052B0E03021A05000414".toCharArray()); //TpmUtils.hexStringToByteArray("3021300906052B0E03021A05000414");
        try {

            log.debug("Verifying the certify key signature against the AIK cert which signed it.");
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, aikPublicKey);
            byte[] signedDigest = cipher.doFinal(certifyKeySignatureBlob);
            byte[] signedDigestWithoutOidPadding = Arrays.copyOfRange(signedDigest, oidPadding.length, signedDigest.length);
            byte[] computedDigest = Sha1Digest.digestOf(certifyKeyDataBlob).toByteArray();

            log.debug("Verifying the signed digest {} against the computed digest {}",
                Hex.encodeHexString(signedDigestWithoutOidPadding),
                Hex.encodeHexString(computedDigest));

            boolean result = Arrays.equals( signedDigestWithoutOidPadding, computedDigest );

            log.debug("Result of signature verification is {}", result);

            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }

    }

    /**
     * Validates the public key digest with that present in the CertifyKey
     * specified.
     *
     * @param publicKeyInfo
     * @param tcgCertificate
     * @return
     * @throws Exception
     */
    public static boolean validatePublicKey(byte[] publicKeyInfo, byte[] tcgCertificate) throws Exception {
        try {
            log.debug("Validating the Public Key.");
            TpmCertifyKey20 tpmCertifyKey = new TpmCertifyKey20(tcgCertificate);

            //Get the public key digest from attestation info
            byte[] providedName = tpmCertifyKey.getTpmuAttest().getTpmsCertifyInfoBlob().getTpmtHa().getDigest();
            log.debug("Provided name in key attestation: {}", TpmUtils.byteArrayToHexString(providedName));

            //remove first two bytes that represent the public area size
            byte[] publicKeyInfoBuffer = Arrays.copyOfRange(publicKeyInfo, 2, publicKeyInfo.length);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] publicKeyInfoBufferDigest = messageDigest.digest(DatatypeConverter.parseHexBinary
                    (TpmUtils.byteArrayToHexString(publicKeyInfoBuffer)));
            log.debug("Public key SHA 256 digest: {}", TpmUtils.byteArrayToHexString(publicKeyInfoBufferDigest));

            return Arrays.equals(providedName, publicKeyInfoBufferDigest);
        } catch (TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException ex) {
            throw ex;
        }
    }


    public static boolean isBindingKey(TpmCertifyKey20 certifiedKey) {
        byte[] TPM_GENERATED = {(byte) 0xff, (byte) 0x54, (byte) 0x43, (byte) 0x47};
        byte[] TPM_ST_ATTEST_CERTIFY = {(byte) 0x80, (byte) 0x17};

        if (!TpmUtils.compareByteArrays(certifiedKey.getMagic(), TPM_GENERATED)) {
            log.debug("Invalid structure, it wasn't created by the TPM, got {}, expecting {}", certifiedKey.getMagic(), TPM_GENERATED);
            return false;
        }
        if (!TpmUtils.compareByteArrays(certifiedKey.getType(), TPM_ST_ATTEST_CERTIFY)) {
            log.debug("Invalid type, not generated by  TPM2_Certify(), got type: {}", certifiedKey.getType());
            return false;
        }
        return true;
    }

    public static boolean isSigningKey(TpmCertifyKey20 certifiedKey) {
        return true;
    }
}
