/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.keystore;

import com.intel.mtwilson.bundle.ZipBundle;
import static com.intel.mtwilson.crypto.jca.ByteArrayUtil.toByteArray;
import static com.intel.mtwilson.crypto.jca.ByteArrayUtil.concat;
import com.intel.mtwilson.crypto.jca.KeyStoreIntegrityException;
import com.intel.mtwilson.crypto.jca.LogUtil;
import com.intel.mtwilson.crypto.jca.StringUtil;
import static com.intel.mtwilson.crypto.jca.StringUtil.UTF8;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 * Every key is in a subdirectory with its metadata:
 *
 * <pre>
 * keystore/
 * + content/
 *   + key1/
 *     + key   (encrypted with password)
 *     + salt
 *     + info
 *     + hmac
 *   + key2/
 *     + key   (encrypted with password)
 *     + pem   (non-encrypted, may have certificate chain for this private key)
 *     + salt
 *     + info
 *     + hmac
 *   + publickey1/
 *     + pem    (non-encrypted)
 *     + salt
 *     + info
 *     + hmac
 *   + certificate1/
 *     + pem    (non-encrypted)
 *     + salt
 *     + info
 *     + hmac
 * + info/
 *   + cryptoform/
 *     + uri
 * + hmac
 * + salt
 * </pre>
 *
 * Can store unencrypted public keys or certificates by using just setKeyInfo
 * without setKey, and still get integrity protection for these:
 *
 * @author jbuhacoff
 */
public class ZipKeyStore {

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZipKeyStore.class);
    private static final LogUtil.Logger log = new LogUtil.Logger();
    private final Directory directory = new Directory();
    private CryptoForm cryptoform;
    private byte[] keystoreSalt;
    private MasterKey keystoreMasterKey;

    /**
     * 
     * @param label
     * @param key
     * @param password must not be null
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException 
     */
    public void putKey(String label, byte[] key, char[] password) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        if( key == null || password == null ) {
            throw new NullPointerException();
        }
        Entry entry = new Entry(label);
        entry.salt = cryptoform.generateSalt();
            // the normal case is to have a pssword, even if it's empty (new char[0]) or set to same as keystore password
            MasterKey keyMasterKey = cryptoform.deriveMasterKeyFromPasswordWithSalt(password, entry.salt);
            EncryptionSecretKey keyEncryptionKey = keyMasterKey.deriveEncryptionSecretKey(entry.salt);
            byte[] keyCiphertext = keyEncryptionKey.encrypt(key);
    //        IntegritySecretKey keystoreIntegrityKey = outputKeystoreMasterKey.deriveIntegritySecretKey(entry.salt);
    //       byte[] hmac = keystoreIntegrityKey.authenticate(message);
            entry.key = keyCiphertext;
    //        entry.hmac = 
        directory.put(entry);
    }

    public void putKeyInfo(String label, String attribute, byte[] info) {
        Entry entry = directory.get(label);
        if (entry == null) {
            entry = new Entry(label);
            directory.put(entry);
        }
        entry.getInfo().put(attribute, info);
    }

    public byte[] getKey(String label, char[] password) throws NoSuchAlgorithmException, InvalidKeyException, UnrecoverableKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException {
        if(  password == null ) {
            throw new NullPointerException();
        }
        Entry entry = directory.get(label);
        if (entry == null) {
            return null;
        }

        
        MasterKey keyMasterKey = cryptoform.deriveMasterKeyFromPasswordWithSalt(password, entry.salt);
        EncryptionSecretKey keyEncryptionKey = keyMasterKey.deriveEncryptionSecretKey(entry.salt);

        log.debug("getKey entry salt: {}", new String(Base64.encode(entry.salt)));

        byte[] keyCiphertext = entry.key;
        byte[] keyPlaintext = keyEncryptionKey.decrypt(keyCiphertext); // decrypt(secretKey(entryKeyEncryptionKey, keyProtectionInfo.getAlgorithmName()), keyCiphertext);
        return keyPlaintext;
    }

    public byte[] getKeyInfo(String label, String attribute) {
        Entry entry = directory.get(label);
        if (entry == null) {
            return null; // or should we throw an entry not found exception ?
        }
        return entry.getInfo().get(attribute);
    }

    /**
     * Deletes the key and all its metadata from the keystore
     *
     * @param label
     */
    public void deleteKey(String label) {
        directory.remove(label);
    }

    public Collection<String> index() {
        return directory.index();
    }

    public void read(InputStream in, char[] password) throws IOException, NoSuchAlgorithmException {
        if (in == null) {
            // default settings for a new empty keystore
            CryptoFormResolver resolver = new CryptoFormResolver();
            cryptoform = resolver.getCryptoForm(KeystoreCryptoForm.KEYSTORE_CRYPTOFORM_URI);
            if (cryptoform == null) {
                log.debug("Keystore Cryptoform URI: {}", KeystoreCryptoForm.KEYSTORE_CRYPTOFORM_URI);
                throw new IOException("Unable to load keystore : Invalid keystore cryptoform uri");
            }
            keystoreSalt = cryptoform.generateSalt();
            keystoreMasterKey = cryptoform.deriveMasterKeyFromPasswordWithSalt(password, keystoreSalt);
            return;
        }
        ZipBundle storage = new ZipBundle();
        storage.read(in);
        // read the metadata
        byte[] salt = storage.get("salt");
        byte[] hmac = storage.get("hmac");
        byte[] cryptoformBytes = storage.get("info/cryptoform/uri");
        if (salt == null || hmac == null || cryptoformBytes == null /*derivationInfoBytes == null || encryptionInfoBytes == null || integrityInfoBytes == null*/) {
            throw new IOException("Metadata not found");
        }

        // load the cryptoform for this keystore
        CryptoFormResolver resolver = new CryptoFormResolver();
        cryptoform = resolver.getCryptoForm(new String(cryptoformBytes, UTF8));
        if (cryptoform == null) {
            log.debug("Keystore Cryptoform URI: {}", new String(cryptoformBytes, UTF8));
            throw new IOException("Unable to load keystore : Invalid keystore cryptoform uri");
        }
        keystoreSalt = salt;
        keystoreMasterKey = cryptoform.deriveMasterKeyFromPasswordWithSalt(password, salt);

        // read all the entries like "label/attribute"
        directory.read(storage);

        // for each entry, read all its content from the bundle
        for (Entry entry : directory) {
            entry.read(storage);
        }

        // compute hmac of entire bundle except for the hmac file
        storage.remove("hmac");
        log.debug("index: {}", StringUtil.join(storage.index(), ","));
        byte[] message = toMessage(storage);
        try {
            IntegritySecretKey keystoreIntegrityKey = keystoreMasterKey.deriveIntegritySecretKey(salt);
            boolean ok = Arrays.equals(hmac, keystoreIntegrityKey.authenticate(message));
            if (!ok) {
                log.debug("stored hmac: {}", new String(Hex.encode(hmac), UTF8));
                log.debug("computed hmac: {}", new String(Hex.encode(keystoreIntegrityKey.authenticate(message)), UTF8));
                throw new KeyStoreIntegrityException("Keystore is corrupt or password is incorrect");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | KeyStoreIntegrityException e) {
            throw new IOException(e);
        }

    }

    public void write(OutputStream out, char[] password) throws IOException {
        ZipBundle storage = new ZipBundle();

        // write the cryptoform; if we need to store with different settings
        // this would be the place where we change cryptoform for the output
        storage.put("info/cryptoform/uri", toByteArray(cryptoform.toURI()));

        // generate a new salt whenever the password is changed.
        byte[] salt = keystoreSalt;
        MasterKey outputKeystoreMasterKey = cryptoform.deriveMasterKeyFromPasswordWithSalt(password, keystoreSalt);
        if (!keystoreMasterKey.equals(outputKeystoreMasterKey)) {
            // the password is different, generate a new salt for the output
            salt = cryptoform.generateSalt();
        }
        storage.put("salt", salt);

        // write each entry to the bundle
        for (Entry entry : directory) {
            entry.write(storage);
        }

        // compute hmac of entire bundle except for the hmac file
        storage.remove("hmac");
        log.debug("index: {}", StringUtil.join(storage.index(), ","));
        byte[] message = toMessage(storage);
        try {
            IntegritySecretKey keystoreIntegrityKey = outputKeystoreMasterKey.deriveIntegritySecretKey(salt);
            byte[] hmac = keystoreIntegrityKey.authenticate(message);
            storage.put("hmac", hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IOException(e);
        }

        storage.write(out);
    }

    public static Integer toInteger(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return Integer.valueOf(text);
    }

    /**
     * @param parts zero or more objects to serialize and concatenate into a
     * single message
     * @return a sequence of (length,data) pairs, there are no delimiters
     * between the pairs
     */
    private static byte[] toMessage(Object... parts) {
        byte[][] arrays = new byte[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                arrays[i] = new byte[0];
            } else if (parts[i] instanceof byte[]) {
                arrays[i] = (byte[]) parts[i];
            } else if (parts[i] instanceof Integer) {
                arrays[i] = toByteArray((Integer) parts[i]);
            } else if (parts[i] instanceof String) {
                arrays[i] = toByteArray((String) parts[i]);
            } else if (parts[i] instanceof char[]) {
                arrays[i] = toByteArray((char[]) parts[i]);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return toMessage(arrays);
    }

    /**
     * @param bundle
     * @return a single byte[] comprised of a sequence of (label.length, label,
     * content.length, content) for each file in the bundle
     */
    private static byte[] toMessage(ZipBundle bundle) {
        ArrayList<String> index = new ArrayList<>();
        index.addAll(bundle.index());
        Collections.sort(index);
        byte[][] lcArrays = new byte[index.size() * 2][];
        int i = 0;
        for (String label : index) {
            log.debug("label {} lcArrays {}, {}", label, i, i + 1);
            lcArrays[i] = toByteArray(label);
            lcArrays[i + 1] = bundle.get(label);
            i += 2;
        }
        return toMessage(lcArrays); // (label,content) => (label.length, label, content.length, content)
    }

    /**
     *
     * @param arrays zero or more byte[] arrays to concatenate into a single
     * message
     * @return a single byte[] comprised of a sequence of (array.length, array)
     * pairs, there are no delimiters between the pairs
     */
    private static byte[] toMessage(byte[]... arrays) {
        byte[][] lvArrays = new byte[arrays.length * 2][];
        int i = 0;
        for (byte[] array : arrays) {
            log.debug("lvArrays {}, {}", i, i + 1);
            lvArrays[i] = toByteArray(array.length);
            lvArrays[i + 1] = array;
            i += 2;
        }
        return concat(lvArrays);
    }

    private static class Directory implements Iterable<Entry> {

        private final HashMap<String, Entry> map = new HashMap<>();

        public void put(Entry entry) {
            map.put(entry.alias, entry);
        }

        public Entry get(String alias) {
            return map.get(alias);
        }

        public void remove(String alias) {
            map.remove(alias);
        }

        /**
         *
         * @return collection of labels of all entries in the directory
         */
        public Collection<String> index() {
            return map.keySet();
        }

        public void read(ZipBundle bundle) {
//            index.clear();
            map.clear();
            for (String filepath : bundle.index()) {
                if (filepath.startsWith("content/")) {
                    String parts[] = filepath.substring("content/".length()).split("/"); // "content/{alias}/attribute" => [alias,attribute]
                    if (parts.length < 2) {
                        log.error("unexpected file in content directory: {}", filepath);
                        continue;
                    }
                    String label = parts[0];
                    String attribute = parts[1];
                    log.debug("alias: {}  attribute: {}", label, attribute);
                    if (!map.containsKey(label)) {
                        map.put(label, new Entry(label));
                    }
                    if ("info".equals(attribute) && parts.length == 3) {
                        String userDefinedAttribute = parts[2];
                        // create placeholders for the entry user-defined attribtues
                        // so that later, when the entry content is read from the bundle,
                        // we can go directly to these paths to read the content.
                        // otherwise, we would need to scan the entire bundle index again
                        // for each entry to identify the attributes to read for that entry
                        log.debug("directory setting placeholder for key {} userDefinedAttribute: {}", label, userDefinedAttribute);
                        Entry entry = map.get(label);
                        entry.getInfo().put(userDefinedAttribute, null); // placeholder for this attribute so that later, when the entry is reading from the bundle, it can look for t
                    }
                }
            }

        }

        @Override
        public Iterator<Entry> iterator() {
            return map.values().iterator();
        }
    }

    private static class Entry {

        private final String alias;
        private byte[] key;     // ciphertext
//        private byte[] pem;     // plaintext, PEM format, UTF-8 encoding
        private final Map<String, byte[]> info = new HashMap<>();    // plaintext
        private byte[] salt;    // plaintext
        private byte[] hmac;    // plaintext, only used to check that password is correct or not, in order to distinguish between wrong password and decryption error

        public Entry(String alias) {
            this.alias = alias;
        }

        // creates a virtual message to use when checking password correctness
        public byte[] message() {
            return toMessage(alias, salt, key);
        }

        /**
         * Example info attributes: "type" like
         * SecretKey,PrivateKey,PublicKey,Certificate "algorithm" like AES, RSA
         * "pem" like PEM-formatted certificate chain for a Private Key
         *
         * @return
         */
        public Map<String, byte[]> getInfo() {
            return info;
        }

        // TODO TBD: implement getCreationTime() to encapsulate infoMap.get("ctime") , Long.valueOf , then new Date(...) ?
        /**
         * Precondition: entry must be initialized with alias
         *
         * @param bundle
         */
        public void write(ZipBundle bundle) {
            if (key != null) {
                bundle.put("content/" + alias + "/key", key);
            }
//            if( pem != null ) { bundle.put("content/"+alias+"/pem", pem); } // TODO:  this should jsut be an info...
            if (hmac != null) {
                bundle.put("content/" + alias + "/hmac", hmac);
            }
            if (salt != null) {
                bundle.put("content/" + alias + "/salt", salt);
            }
            for (Map.Entry<String, byte[]> attribute : info.entrySet()) {
                byte[] attributeValue = attribute.getValue();
                if (attributeValue != null) {
                    bundle.put("content/" + alias + "/info/" + attribute.getKey(), attributeValue);
                }
            }
        }

        /**
         * Precondition 1: entry must be initialized with alias Precondition 2:
         * if the entry has any user-defined attributes, the attribute names are
         * already in the info map with null values
         *
         * @param bundle
         */
        public void read(ZipBundle bundle) {
            key = bundle.get("content/" + alias + "/key");
//            pem = bundle.get("content/"+alias+"/pem"); // TODO: this should just be an info
            ArrayList<String> infoAttributeNames = new ArrayList<>();
            infoAttributeNames.addAll(info.keySet());
            for (String attribute : infoAttributeNames) {
                log.debug("looking for key {} attribute {} value in bundle", alias, attribute);
                byte[] attributeValue = bundle.get("content/" + alias + "/info/" + attribute);
                info.put(attribute, attributeValue);
            }
            hmac = bundle.get("content/" + alias + "/hmac");
            salt = bundle.get("content/" + alias + "/salt");
        }
    }

}
