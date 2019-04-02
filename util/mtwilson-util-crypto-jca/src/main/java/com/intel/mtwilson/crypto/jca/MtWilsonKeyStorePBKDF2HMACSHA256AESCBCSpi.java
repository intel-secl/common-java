/*
 * Copyright 2017 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.crypto.jca;

import static com.intel.mtwilson.crypto.jca.StringUtil.UTF8;
import com.intel.mtwilson.crypto.keystore.ZipKeyStore;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import org.bouncycastle.util.encoders.Base64;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * A KeyStore implementation using PBKDF2, HMAC-SHA256, and AES-CBC with PKCS #7
 * padding.
 *
 * The keystore integrity is protected using HMAC-SHA256 over the index of keys
 * present in the keystore, and also separately over of the content of each key
 * and its metadata.
 *
 * When the keystore is opened, the index integrity is verified. When individual
 * keys are accessed, their integrity is verified.
 *
 * The HMAC key is derived using HKDF(PBKDF2(password)) with a keystore salt.
 * The keystore salt, and also each key hmac and the keystore index hmac, are
 * updated every time the keystore is written to an output stream. The keystore
 * that is already in memory is NOT modified in any way when this happens.
 *
 * Secret keys and private keys are encrypted with a per-key encryption key. The
 * key encryption key is derived using HKDF(PBKDF2(password)) with a per-key
 * salt. The per-key salt is updated every time the key entry is updated (NOT
 * every time the keystore is written to output stream).
 *
 * File format is .zip using Java built-in ZipInputStream and ZipOutputStream
 * Inside the .zip file, each entry is a directory with a set of files to store
 * the encrypted key, the certificate chain, metadata, salt, and hmac values.
 *
 * The metadata for each key includes the key type and the key algorithm (as
 * specified by the application - not the one we use for key encryption).
 *
 * The Java KeyStore API allows for certificate entries that are associated with
 * keys and for stand-alone certificate entries. If a keystore has both key and
 * pem entries then the certificate is associated with the key. If a keystore
 * has only a pem entry for an alias then it's a stand-alone certificate entry.
 *
 * Entry creation dates are stored in key info block as a long integer obtained
 * using Date getTime(), representing milliseconds since January 1, 1970,
 * 00:00:00 GMT.
 *
 * Summary of keystore directory structure, where `{alias}` is a placeholder for
 * the alias of the entry and there would be one of these directories for each
 * entry:
 *
 * <pre>
 * keystore.mtwks
 * + hmac     (integrity check for the keystore - the info and list of aliases)
 * + info     (contains key encryption settings for entire keystore)
 * + salt
 * + {alias}
 *   + hmac   (integrity check for this entry)
 *   + info   (contains metadata for this entry: entry type and user-specified algorithm name)
 *   + key    (the encrypted key)
 *   + salt
 * </pre>
 *
 * @author jbuhacoff
 */
public class MtWilsonKeyStorePBKDF2HMACSHA256AESCBCSpi extends KeyStoreSpi {

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonKeyStorePBKDF2HMACSHA256AESCBCSpi.class);
    private static final LogUtil.Logger log = new LogUtil.Logger();
    private final ZipKeyStore keystore = new ZipKeyStore();

    public MtWilsonKeyStorePBKDF2HMACSHA256AESCBCSpi() {
        super();
    }

    /*
    private static void clear(char[] space) {
        Arrays.fill(space, '\u0000');
    }

    private static void clear(byte[] space) {
        Arrays.fill(space, (byte) 0x00);
    }
     */
    private static SecretKey secretKey(byte[] secretKeyBytes, String algorithm) {
        return new SecretKeySpec(secretKeyBytes, algorithm);
    }

    private static PrivateKey privateKey(byte[] privateKeyBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(algorithm); // throws NoSuchAlgorithmException
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes)); // throws InvalidKeySpecException
        return privateKey;
    }

    private static PublicKey publicKey(byte[] publicKeyBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(algorithm); // throws NoSuchAlgorithmException
        PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicKeyBytes)); // throws InvalidKeySpecException
        return publicKey;
    }

    private static X509Certificate certificate(byte[] certificateBytes) throws CertificateException {
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        return cert;
    }

    /**
     * Returns the key associated with the given alias, using the given password
     * to recover it.
     *
     * @param alias
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            byte[] keyBytes = keystore.getKey(alias, password);
            byte[] typeBytes = keystore.getKeyInfo(alias, "type");
            byte[] algorithmBytes = keystore.getKeyInfo(alias, "algorithm");
            if (keyBytes == null || typeBytes == null || algorithmBytes == null) {
                log.debug("keybytes:{}, typeBytes:{}, algorithmBytes:{}", keyBytes, typeBytes, algorithmBytes);
                throw new UnrecoverableKeyException("Missing key or key info");
            }
            String type = new String(typeBytes, UTF8);
            String algorithm = new String(algorithmBytes, UTF8);
            if (type == null) {
                throw new UnrecoverableKeyException("Unknown key type");
            }
            switch (type) {
                case "PrivateKey":
                    return privateKey(keyBytes, algorithm);
                case "PublicKey":
                    return publicKey(keyBytes, algorithm);
                case "SecretKey":
                    return secretKey(keyBytes, algorithm);
                default:
                    throw new UnrecoverableKeyException("Unknown key type");
            }
        } catch (InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            log.debug("Caught exception in engineGetKey for alias: {}", alias, e);
            throw new UnrecoverableKeyException(e.getMessage());
        }
    }

    private static List<Certificate> getCertificates(byte[] pem) throws CertificateException, IOException {
        ArrayList<Certificate> certificates = new ArrayList<>();
        PemReader reader = new PemReader(new StringReader(new String(pem, UTF8)));
        PemObject object = reader.readPemObject(); // throws IOException
        while (object != null) {
            log.debug("read pem object type: {}", object.getType());
            if (object.getType().contains("CERTIFICATE")) {
                certificates.add(certificate(object.getContent())); // throws CertificateException
            } else {
                log.error("unrecognized pem content type: {}", object.getType());
            }
            object = reader.readPemObject();
        }
        return certificates;
    }

    /**
     * Returns the certificate chain associated with the given alias.
     *
     * @param alias
     * @return
     */
    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        try {
            byte[] pemBytes = keystore.getKeyInfo(alias, "pem");
            byte[] typeBytes = keystore.getKeyInfo(alias, "type");
            if (pemBytes == null || typeBytes == null) {
                return null;
            }
            String type = new String(typeBytes, UTF8);
            if (type.equalsIgnoreCase("PrivateKey") || type.equalsIgnoreCase("Certificate")) {
                // read the certificates from pem file into array
                List<Certificate> certificates = getCertificates(pemBytes);
                return certificates.toArray(new Certificate[0]);
            }
            log.error("Not a private key or certificate entry: {}", type);
            return null;
        } catch (IOException | CertificateException e) {
            throw new IllegalStateException("Cannot load key info", e);
        }
    }

    /**
     * Returns the certificate associated with the given alias.
     *
     * @param alias
     * @return
     */
    @Override
    public Certificate engineGetCertificate(String alias) {
        Certificate[] chain = engineGetCertificateChain(alias);
        if (chain == null || chain.length == 0) {
            return null;
        }
        return chain[0];
    }

    /**
     * Returns the creation date of the entry identified by the given alias.
     *
     * @param alias
     * @return
     */
    @Override
    public Date engineGetCreationDate(String alias) {
        try {
            byte[] ctimeBytes = keystore.getKeyInfo(alias, "ctime");
            if (ctimeBytes == null) {
                return null;
            }
            Long ctime = Long.valueOf(new String(ctimeBytes, "UTF8"));
            return new Date(ctime);
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            log.debug("Caught error in engineGetCreationDate: {}", alias, e);
            return null;
        }
    }

    private static String pem(Certificate[] chain) throws IOException, CertificateEncodingException {
        StringWriter buffer = new StringWriter();
        try (PemWriter writer = new PemWriter(buffer)) {
            for (Certificate cert : chain) {
                log.debug("writing PEM certificate type {}", cert.getType());
                PemObject pemObject = new PemObject("CERTIFICATE", cert.getEncoded());
                writer.writeObject(pemObject);
            }
        }
        return buffer.toString();
    }

    /**
     * Assigns the given key to the given alias, protecting it with the given
     * password.
     *
     * @param alias
     * @param key
     * @param password
     * @param chain
     * @throws KeyStoreException
     */
    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        log.debug("engineSetKeyEntry with password, using specified password");
        try {
            // if there was something already there under the same alias, remove it first
            keystore.deleteKey(alias);
            log.debug("engineSetKeyEntry(Key) alias:{} key:{}", alias, new String(Base64.encode(key.getEncoded())));
            if (key instanceof PrivateKey) {
                PrivateKey privateKey = (PrivateKey) key;
                keystore.putKey(alias, privateKey.getEncoded(), password);
                keystore.putKeyInfo(alias, "type", "PrivateKey".getBytes(UTF8));
                keystore.putKeyInfo(alias, "algorithm", key.getAlgorithm().getBytes(UTF8));
                keystore.putKeyInfo(alias, "format", key.getFormat().getBytes(UTF8));
            } else if (key instanceof SecretKey) {
                SecretKey secretKey = (SecretKey) key;
                keystore.putKey(alias, secretKey.getEncoded(), password);
                keystore.putKeyInfo(alias, "type", "SecretKey".getBytes(UTF8));
                keystore.putKeyInfo(alias, "algorithm", key.getAlgorithm().getBytes(UTF8));
                keystore.putKeyInfo(alias, "format", key.getFormat().getBytes(UTF8));
            } else if (key instanceof PublicKey) {
                PublicKey publicKey = (PublicKey) key;
                keystore.putKey(alias, publicKey.getEncoded(), password);
                keystore.putKeyInfo(alias, "type", "PublicKey".getBytes(UTF8));
                keystore.putKeyInfo(alias, "algorithm", key.getAlgorithm().getBytes(UTF8));
                keystore.putKeyInfo(alias, "format", key.getFormat().getBytes(UTF8));
            } else {
                throw new UnsupportedOperationException(String.format("Cannot store key of type: %s", key.getClass().getName()));
            }
            if (chain != null) {
                keystore.putKeyInfo(alias, "pem", pem(chain).getBytes(UTF8));
            }
        } catch (IOException | CertificateEncodingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        }
    }

    /**
     * Assigns the given key (that has already been protected) to the given
     * alias.
     *
     * @param alias
     * @param key
     * @param chain
     * @throws KeyStoreException
     */
    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        log.debug("engineSetKeyEntry with no password, using keystore password");
        try {
            // if there was something already there under the same alias, remove it first
            keystore.deleteKey(alias);
            log.debug("engineSetKeyEntry(byte[]) alias:{} key:{}", alias, new String(Base64.encode(key)));
            keystore.putKey(alias, key, null); // no password means we want to store it without further encryption (because it's already encrypted)
            if (chain != null) {
                keystore.putKeyInfo(alias, "pem", pem(chain).getBytes(UTF8));
            }
        } catch (IOException | CertificateEncodingException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        }
    }

    /**
     * Assigns the given certificate to the given alias.
     *
     * @param alias
     * @param cert
     * @throws KeyStoreException
     */
    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        log.debug("engineSetCertificateEntry");
        try {
            // if there was something already there under the same alias, remove it first
            keystore.deleteKey(alias);
            // store the certificate in pem format; there is no key entry
            keystore.putKeyInfo(alias, "type", "Certificate".getBytes(UTF8));
            keystore.putKeyInfo(alias, "pem", pem(new Certificate[]{cert}).getBytes(UTF8));
        } catch (IOException | CertificateEncodingException e) {
            throw new KeyStoreException(e);
        }
    }

    /**
     * Deletes the entry identified by the given alias from this keystore.
     *
     * @param alias
     * @throws KeyStoreException
     */
    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        keystore.deleteKey(alias);
    }

    /**
     * Lists all the alias names of this keystore.
     *
     * @return
     */
    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(keystore.index());
    }

    /**
     * Checks if the given alias exists in this keystore.
     *
     * @param alias
     * @return
     */
    @Override
    public boolean engineContainsAlias(String alias) {
        return keystore.index().contains(alias);
    }

    /**
     * Retrieves the number of entries in this keystore.
     *
     * @return
     */
    @Override
    public int engineSize() {
        return keystore.index().size();
    }

    /**
     * Returns true if the entry identified by the given alias was created by a
     * call to setKeyEntry, or created by a call to setEntry with a
     * PrivateKeyEntry or a SecretKeyEntry.
     *
     * @param alias
     * @return
     */
    @Override
    public boolean engineIsKeyEntry(String alias) {
        try {
            byte[] typeBytes = keystore.getKeyInfo(alias, "type");
            if (typeBytes == null) {
                return false;
            }
            String type = new String(typeBytes, UTF8);
            return type.equalsIgnoreCase("PrivateKey") || type.equalsIgnoreCase("SecretKey");
        } catch (Exception e) {
            log.debug("Caught error in engineIsKeyEntry: {}", alias, e);
            return false;
        }
    }

    /**
     * Returns true if the entry identified by the given alias was created by a
     * call to setCertificateEntry, or created by a call to setEntry with a
     * TrustedCertificateEntry.
     *
     * @param alias
     * @return
     */
    @Override
    public boolean engineIsCertificateEntry(String alias) {
        try {
            byte[] typeBytes = keystore.getKeyInfo(alias, "type");
            if (typeBytes == null) {
                return false;
            }
            String type = new String(typeBytes, UTF8);
            return type.equalsIgnoreCase("Certificate");
        } catch (Exception e) {
            log.debug("Caught error in engineIsCertificateEntry: {}", alias, e);
            return false;
        }
    }

    private static boolean isCertificateInList(Certificate query, List<Certificate> list) throws CertificateEncodingException {
        for (Certificate certificate : list) {
            if (Arrays.equals(certificate.getEncoded(), query.getEncoded())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the (alias) name of the first keystore entry whose certificate
     * matches the given certificate.
     *
     * @param cert
     * @return
     */
    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String found = null;

        for (String alias : keystore.index()) {
            try {
                byte[] typeBytes = keystore.getKeyInfo(alias, "type");
                if (typeBytes == null) {
                    continue;
                }
                String type = new String(typeBytes, UTF8);
                if (type.equalsIgnoreCase("Certificate") || type.equalsIgnoreCase("PrivateKey")) {
                    try {
                        byte[] pemBytes = keystore.getKeyInfo(alias, "pem");
                        if (pemBytes == null) {
                            continue;
                        }
                        List<Certificate> certificates = getCertificates(pemBytes);
                        if (certificates == null || certificates.isEmpty()) {
                            continue;
                        }
                        Certificate firstCertificate = certificates.get(0);
                        if (Arrays.equals(firstCertificate.getEncoded(), cert.getEncoded())) {
                            found = alias;
                            break;
                        }
                    } catch (CertificateException | IOException e) {
                        log.error("Cannot read certificate", e);
//                    continue;
                    }
                }
                /*else {
                // ignore other entry types
                continue;
            }*/
            } catch (Exception e) {
                log.debug("Caught error in engineGetCertificateAlias: {}", alias, e);
            }
        }
        return found;
    }

    /**
     * Stores this keystore to the given output stream, and protects its
     * integrity with the given password.
     *
     * @param stream
     * @param password
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        keystore.write(stream, password);
    }

    /**
     * Loads the keystore from the given input stream.
     *
     * @param stream
     * @param password
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        keystore.read(stream, password);
    }

}
