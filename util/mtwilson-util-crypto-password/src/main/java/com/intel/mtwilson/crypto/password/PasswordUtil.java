/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.password;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.Sha384Digest;
import com.intel.dcsg.cpg.io.ByteArray;

/**
 * This class is similar to HashedCredentialsMatcher that comes wtih 
 * Apache Shiro but instead of having a static configuration of 
 * the algorithm name and iteration count (which requires downtime
 * while upgrading passwords on the server for all accounts), 
 * this matcher allows a per-instance configuration using the
 * corresponding PasswordAuthenticationInfo class used by the
 * JdbcPasswordRealm 
 * 
 * @author jbuhacoff
 */
public class PasswordUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordUtil.class);
    
    public static byte[] hash(byte[] inputPasswordBytes, HashProtection hashProtection) {
        // SHA-384 is the standard Java name but we also accept SHA384
        if( "SHA-384".equalsIgnoreCase(hashProtection.getAlgorithm()) ||  "SHA384".equalsIgnoreCase(hashProtection.getAlgorithm()) ) {
            // first iteration is mandatory
            Sha384Digest digest = Sha384Digest.digestOf(ByteArray.concat(hashProtection.getSalt(), inputPasswordBytes));
            int max = hashProtection.getIterations() - 1; // -1 because we just completed the first iteration
            for(int i=0; i<max; i++) {
                digest = Sha384Digest.digestOf(digest.toByteArray());
            }
            return digest.toByteArray();
        } else if( "SHA-256".equalsIgnoreCase(hashProtection.getAlgorithm()) ||  "SHA256".equalsIgnoreCase(hashProtection.getAlgorithm()) ) {
            // first iteration is mandatory
            Sha256Digest digest = Sha256Digest.digestOf(ByteArray.concat(hashProtection.getSalt(), inputPasswordBytes));
            int max = hashProtection.getIterations() - 1; // -1 because we just completed the first iteration
            for(int i=0; i<max; i++) {
                digest = Sha256Digest.digestOf(digest.toByteArray());
            }
            return digest.toByteArray();
        }
        throw new UnsupportedOperationException("Algorithm not supported: "+hashProtection.getAlgorithm()); 
    }
}
