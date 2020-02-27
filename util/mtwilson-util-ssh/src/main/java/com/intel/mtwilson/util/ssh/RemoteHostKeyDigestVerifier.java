/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * This verifier always succeeds, so the connection is made, but it saves
 * the remote key information so the user can verify it later and then
 * (hopefully) terminate the connection if the key did not check out.
 * This is useful in conjunction with a UI which displays the remote host
 * key and asks the user to verify. We need to save that key for the UI
 * to display, without driving the UI from here.
 * You can either provide a reference to your own RemoteHostKey object which
 * will be populated, or you can use the non-arg constructor and then call
 * getRemoteHostKey() to get a new populated object.
 * 
 * @author jbuhacoff
 */
public class RemoteHostKeyDigestVerifier implements HostKeyVerifier {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoteHostKeyDigestVerifier.class);

    private final String host;
    private final Integer port;
    private final String algorithm;
    private final String hexDigest;

    /**
     * Using this constructor only for isolated situations like junit tests, 
     * because if used in production it would attempt to match ANY host against
     * the given public key and that would only work for one host and reject
     * connections to all others, even if another host key verifier is added
     * that would have approved them... because it just depends on the order
     * they are checked.
     * @param algorithm
     * @param hexDigest 
     */
    public RemoteHostKeyDigestVerifier(String algorithm, String hexDigest) {
        this.host = null;
        this.port = null;
        this.algorithm = algorithm;
        this.hexDigest = hexDigest;
    }

    public RemoteHostKeyDigestVerifier(String host, Integer port, String algorithm, String hexDigest) {
        this.host = host;
        this.port = port;
        this.algorithm = algorithm;
        this.hexDigest = hexDigest;
    }
    

    @Override
    public boolean verify(String host, int port, PublicKey publicKey) {
        if( this.host != null && !this.host.equals(host)) { return false; }
        if( this.port != null && !this.port.equals(port)) { return false; }
        if( publicKey instanceof RSAPublicKey ) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
            byte[] sshEncodedPublicKey = SshUtils.encodeSshRsaPublicKey(rsaPublicKey);
            String remoteHexDigest = Digest.algorithm(algorithm).digest(sshEncodedPublicKey).toHex();
            return hexDigest.equalsIgnoreCase(remoteHexDigest);
        }
        else {
            log.error("Unsupported public key class: {}", publicKey.getClass().getName());
            return false;
        }
    }

}
