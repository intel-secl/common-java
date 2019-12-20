/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.io.*;
import java.security.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.spec.ECGenParameterSpec;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;

import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.io.pem.PemLikeParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import java.security.spec.InvalidKeySpecException;

/**
 * You can also use Java's "keytool" command on your platform to generate keys
 * and add them to a keystore file. Set the "mtwilson.api.keystore" property to
 * point to this file (by default keystore.jks)
 *
 * @since 2.0
 * @author skamal 
 */
public class EcUtil {

    private static Logger log = LoggerFactory.getLogger(EcUtil.class);

/*    public static AsymmetricCipherKeyPair generateEccKeyPair(int keySizeInBits) {
        // Get domain parameters for example curve secp256r1
        X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");
        ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), 
                                                                 ecp.getH(), ecp.getSeed());
        AsymmetricCipherKeyPair keyPair;
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyGenParams);
        keyPair = generator.generateKeyPair();

        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
        byte[] privateKeyBytes = privateKey.getD().toByteArray();
        /* First print our generated private key and public key
        System.out.println("Private key: " + toHex(privateKeyBytes));
        System.out.println("Public key: " + toHex(publicKey.getQ().getEncoded(true)));*/
        //return keyPair;
//    }
    
    public static KeyPair generateEcKeyPair(String curveType) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC","BC");

    ECGenParameterSpec ecsp = new ECGenParameterSpec(curveType);
    kpg.initialize(ecsp, new SecureRandom());

    KeyPair kpU = kpg.generateKeyPair();
    return kpU;
    }
    /**
     * Given some text, this method extracts the first PRIVATE KEY block (PEM-like format) and deserializes the
     * private key, returning a PrivateKey object.
     * This means you can have one file containing both PRIVATE KEY and PUBLIC KEY blocks and extract each
     * key using the corresponding decode method.
     *
     * XXX TODO maybe this method should allow providing a password for decrypting a password-encrypted private key
     * @param text
     * @return
     * @throws CryptographyException
     * @since 0.1.3
     */
    public static PrivateKey decodePemPrivateKey(String text) throws CryptographyException {
    log.debug("decodePemPrivateKey");

        List<Pem> list = PemLikeParser.parse(text);
        for(Pem pem : list) {
            ///TODO: not removed as we might deicde to use PKCS#1 format as well.
            ///if( ("PRIVATE KEY".equals(pem.getBanner())) || ("EC PRIVATE KEY".equals(pem.getBanner()))) {
            if( ("PRIVATE KEY".equals(pem.getBanner()))) {
                byte[] der = pem.getContent();
                return decodeDerPrivateKey(der);
            }
        }
        return null;
    }

    public static PrivateKey decodeDerPrivateKey(byte[] privateKeyBytes) throws CryptographyException {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory factory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = factory.generatePrivate(spec);
            return privateKey;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }

    public static PublicKey extractPublicKey(PrivateKey privateKey, String curveType) throws CryptographyException {
    log.debug("generatePublicKey");
    try {
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(curveType);

        ECPoint Q = ecSpec.getG().multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey)privateKey).getD());

        ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
        PublicKey publicKeyGenerated = keyFactory.generatePublic(pubSpec);
        //this.publicKey = publicKeyGenerated;
        return publicKeyGenerated;
        } catch(NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
	    throw new CryptographyException(e);
        } catch (Exception e) {
            throw new CryptographyException(e);
        }
    }
}
