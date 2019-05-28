package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.HexUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Representation of a single SHA384 Digest. An SHA384 Digest is a 48-byte value.
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class Sha384Digest extends AbstractDigest {
    private static final DigestAlgorithm ALGORITHM = DigestAlgorithm.SHA384;

    public final static Sha384Digest ZERO = new Sha384Digest(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});

    /**
     * This constructor exists so the valueOf methods can instantiate an SHA384 object
     * and set its value after validating the input. If they were to call the public
     * constructors, the validation would happen again there redundantly.
     */
    protected Sha384Digest() {
        super(ALGORITHM);
    }

    /**
     * Use this constructor if you expect that the bytes are a valid SHA384 digest.
     * If they are not a valid SHA384 digest an IllegalArgumentException exception will be thrown.
     *
     * If you need to create a Sha384Digest where the input may be invalid, use valueOf.
     *
     * @param value
     */
    public Sha384Digest(byte[] value) {
        super(ALGORITHM, value);
    }

    /**
     * Use this constructor if you expect that the hex string represents a valid SHA384 digest.
     * If it does not look like an SHA384 digest an IllegalArgumentException exception will be thrown.
     *
     * If you need to create a Sha384Digest where the input may be invalid, use valueOf.
     *
     * @param hex
     */
    public Sha384Digest(String hex) {
        super(ALGORITHM, hex);
    }

    /**
     * Creates a NEW instance of Sha384Digest that contains the result of
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     * @since 0.2
     */
    public Sha384Digest extend(Sha384Digest data) {
        return extend(data.toByteArray());
    }

    /**
     * Creates a NEW instance of Sha384Digest that contains the result of
     * extending this value with the specified data.
     * @param data
     * @return a new instance with the extended value, or null if there was an error
     * @since 0.2
     */
    public Sha384Digest extend(byte[] data) {
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-384"); // cannot use algorithm() because it returns SHA384 not the SHA-384 required by java
            hash.update(toByteArray());
            hash.update(data);
            return new Sha384Digest(hash.digest());
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm: "+algorithm(), e);
        }
    }

    /**
     *
     * @param value can be null
     * @return true if value is a valid SHA384 digest
     */
    public static boolean isValid(byte[] value) {
        return ALGORITHM.isValid(value);
    }

    /**
     * @param hexValue without any punctuation or spaces; can be null
     * @return true if the value is a valid hex representation of an SHA384 digest
     */
    public static boolean isValidHex(String hexValue) {
        return ALGORITHM.isValidHex(hexValue);
    }

    /**
     * @param base64Value value without any punctuation or spaces; can be null
     * @return true if the value is a valid base64 representation of an SHA384 digest
     */
    public static boolean isValidBase64(String base64Value) {
        return ALGORITHM.isValidBase64(base64Value);
    }

    /**
     * Assumes the input represents an SHA384 digest and creates a new instance of Sha384Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA384 representation, a null
     * will be returned.
     *
     * Callers must always check the return value for null.
     *
     * @param digest
     * @return
     */
    public static Sha384Digest valueOf(byte[] digest) {
        if( isValid(digest) ) {
            Sha384Digest SHA384 = new Sha384Digest();
            SHA384.value = digest;
            return SHA384;
        }
        return null;
    }

    /**
     * Assumes the input represents an SHA384 digest and creates a new instance of Sha384Digest to wrap it.
     * This method does NOT compute a digest. If the input is not a valid SHA384 representation, a null
     * will be returned.
     *
     * Callers must always check the return value for null.
     *
     * The input can be either hex or base64
     *
     * @param text either hex or base64 encoded sha384 digest
     * @return
     */
    //@org.codehaus.jackson.annotate.JsonCreator // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonCreator // jackson 2.x
    public static Sha384Digest valueOf(String text) {
        if( isValidHex(text) ) {
            Sha384Digest digest = new Sha384Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        if( isValidBase64(text) ) {
            Sha384Digest digest = new Sha384Digest();
            digest.value = Base64Util.toByteArray(text);
            return digest;
        }
        return null;
    }

    public static Sha384Digest valueOfHex(String text) {
        if( isValidHex(text) ) {
            Sha384Digest digest = new Sha384Digest();
            digest.value = HexUtil.toByteArray(text); // throws HexFormatException if invalid, but shouldn't happen since we check isValid() first
            return digest;
        }
        return null;
    }

    public static Sha384Digest valueOfBase64(String text) {
        if( isValidBase64(text) ) {
            Sha384Digest digest = new Sha384Digest();
            digest.value = Base64Util.toByteArray(text);
            return digest;
        }
        return null;
    }

    /**
     * Computes the SHA384 digest of the input and returns the result.
     * @param message
     * @return
     */
    public static Sha384Digest digestOf(byte[] message) {
        return new Sha384Digest(ALGORITHM.digest(message));
    }
}
