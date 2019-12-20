/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.digest;

/**
 *
 * @author jbuhacoff
 */
public class DigestUtil {
    
    /**
     * 
     * @param algorithm like MD5, sha1, or sha2-256, or sha-384, or sha-2-512
     * @return standard Java algorithm name for given algorithm, or null if no match was found
     */
    public static String getJavaAlgorithmName(String algorithm) {
        String name = algorithm.trim().replace("-", "");
        if( name.equalsIgnoreCase("md5") ) { return "MD5"; }
        if( name.equalsIgnoreCase("sha1") ) { return "SHA-1"; }
        if( name.equalsIgnoreCase("sha256") || name.equalsIgnoreCase("sha2256") ) { return "SHA-256"; }
        if( name.equalsIgnoreCase("sha384") || name.equalsIgnoreCase("sha2384") ) { return "SHA-384"; }
        if( name.equalsIgnoreCase("sha512") || name.equalsIgnoreCase("sha2512") ) { return "SHA-512"; }
        return null;
    }

    /**
     * 
     * @param algorithm
     * @return the name of the algorithm as a prefix, but not the full prefix because we omit the ":" after the algorithm name here
     */
    public static String getPrefixName(String algorithm) {
        return algorithm.trim().replace("-", "").toLowerCase(); // "SHA-256" becomes "sha256"
    }
    
}
