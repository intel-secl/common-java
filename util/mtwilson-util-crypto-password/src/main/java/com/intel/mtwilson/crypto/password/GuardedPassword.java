/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.password;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class GuardedPassword {
    private SecureRandom random = new SecureRandom();

    /* TODO: Secure information NOT to be maintained, but discarded;
    Being done to avoid intrusive design changes for 1.4 Release */
    private char[] insPwd;
    private byte[] secPwd;
    private byte[] salt;

    /* Set password to new value passed */
    public void setPassword(String inPass)
            throws IOException, GeneralSecurityException  {
        /* TODO: Dont take String, take char[] instead */
        /* TODO: Dispose off information on use */
        insPwd = inPass.toCharArray();

        salt = new byte[12];
        random.nextBytes(salt);
        /* TODO: Salt to be stored in a secure storage in case
        of long life usage else return it here */
        //saveBytes(salt, "salt.bin");

        secPwd = hashPassword( insPwd, salt);
        /* TODO: Secure password to be stored in a secure storage
        in case of long life usage else return it here */
        /* saveBytes(hashVal,"password.bin"); */

        /* TODO: Not clearing anything for now */
        /* Arrays.fill(secPwd, (byte) 0); */
    }

    /* Indicates if given password is correct */
    public boolean validatePassword(char[] inPass)
            throws IOException, GeneralSecurityException  {

        if ((secPwd == null) || (salt == null))
            return false;

        /* TODO: Load salt & secure password from secure storage */
        /* byte[] salt = loadBytes("salt.bin"); */

        byte[] hpass = hashPassword( inPass, salt);
        //byte[] hashVal2 = loadBytes("password.bin");

        /* Validate password */
        boolean arraysEqual = timingEquals( hpass, secPwd);
        Arrays.fill(hpass, (byte) 0);
        return arraysEqual;
    }

    /* TODO: This should be removed with needed design changes */
    public String getInsPassword() {
      return (insPwd != null)?String.valueOf(insPwd) : "";
    }

    /* TODO: This should be removed with needed design changes */
    public boolean isPasswordValid() {
        return ((secPwd != null ) && (secPwd.length != 0)) ? true : false;
    }

    /* TODO: This should be removed with needed design changes */
    public String getSecPassword() {
        return Objects.toString(secPwd,"");
    }

    /* TODO: Not required with implementation of auto dispose on read */
    public void dispose() {
        Arrays.fill(secPwd, (byte) 0);
        Arrays.fill(insPwd, Character.MIN_VALUE);
        Arrays.fill(salt, (byte) 0);
        secPwd = salt = null;
        insPwd = null;
    }

    /* Encrypts password & salt and zeroes both */
    private byte[] hashPassword(char[] pass, byte[] salt)
            throws GeneralSecurityException {
        KeySpec spec = new PBEKeySpec(pass, salt, 65536, 128);
        //Arrays.fill(pass, (char) 0);
        //Arrays.fill(salt, (byte) 0);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return f.generateSecret(spec).getEncoded();
    }

    /**
     * Validates if both byte arrays are equal but uses same amount
     * of time if they are the same or different to prevent timing attacks
     */
    private static boolean timingEquals(byte b1[], byte b2[]) {
        boolean result = true;
        int len = b1.length;
        if (len != b2.length) {
            result = false;
        }
        if (len > b2.length) {
            len = b2.length;
        }
        for (int i = 0; i < len; i++) {
            result &= (b1[i] == b2[i]);
        }
        return result;
    }

    /* TODO: Methods for later enhancement */
    /*private void saveBytes(byte[] bytes, String filename) throws IOException {
        // ... write bytes to secure store
    }

    private byte[] loadBytes(String filename) throws IOException {
        // ... read bytes from secure store
    }*/
}