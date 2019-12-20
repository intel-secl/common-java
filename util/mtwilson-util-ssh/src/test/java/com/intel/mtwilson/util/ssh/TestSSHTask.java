/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.PropertiesUtil;
import com.intel.dcsg.cpg.performance.Observer;
import com.intel.dcsg.cpg.performance.Progress;
import com.intel.mtwilson.core.junit.Env;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestSSHTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestSSHTask.class);
    private static Endpoint endpoint;
    private static Password password;
    private static String publicKeyDigest;

    @BeforeClass
    public static void init() throws IOException {
        Properties properties = PropertiesUtil.removePrefix(Env.getProperties("cit3-test-ssh"), "cit3.test.ssh.");
        endpoint = new Endpoint(properties.getProperty("host")); // assumes port 22, username root
        password = new Password(properties.getProperty("password"));
        publicKeyDigest = properties.getProperty("publicKeyDigest");
    }

    @Test
    public void testNewRemoteSSH() throws Exception {
        try (RetryableSchmizzSSHClientFactory ssh = new RetryableSchmizzSSHClientFactory(endpoint, password, publicKeyDigest)) {
            log.debug("factory: {}", ssh.toString());
            ByteArrayOutput buffer = new ByteArrayOutput();
            Exit status = ssh.execute("/bin/ls", buffer);
            String output = new String(buffer.getOutputBytes());
            log.debug("result code {} stdout {}", status.getCode(), output);
        }
    }

    @Test
    public void testNewRemoteSFTP1() throws Exception {
        ByteArraySourceFile file = new ByteArraySourceFile("test-sftp.txt", "hello world\n".getBytes(Charset.forName("UTF-8")));
        try (RetryableSchmizzSSHClientFactory ssh = new RetryableSchmizzSSHClientFactory(endpoint, password, publicKeyDigest)) {
            SFTPClient sftp = ssh.connect().newSFTPClient();
            SFTPFileTransfer transfer = sftp.getFileTransfer();
            transfer.upload(file, file.getName());
        }
    }
    
    @Test
    public void testNewRemoteSFTP1WithProgress() throws Exception {
        ByteArraySourceFile file = new ByteArraySourceFile("test-sftp.txt", "hello world\n".getBytes(Charset.forName("UTF-8")));
        try (RetryableSchmizzSSHClientFactory ssh = new RetryableSchmizzSSHClientFactory(endpoint, password, publicKeyDigest)) {
            SFTPClient sftp = ssh.connect().newSFTPClient();
            SFTPFileTransfer transfer = sftp.getFileTransfer();
            RetryableSchmizzSSHClientFactory.SFTPTransferProgress progressListener = new RetryableSchmizzSSHClientFactory.SFTPTransferProgress(new LoggingProgressObserver());
            transfer.setTransferListener(progressListener);
            transfer.upload(file, file.getName());
        }
    }
    
    @Test
    public void testNewRemoteSFTP2() throws Exception {
        ByteArraySourceFile file = new ByteArraySourceFile("test-sftp.txt", "hello world\n".getBytes(Charset.forName("UTF-8")));
        try (RetryableSchmizzSSHClientFactory ssh = new RetryableSchmizzSSHClientFactory(endpoint, password, publicKeyDigest)) {
            ssh.upload(file, file.getName(), null);
        }
    }

    @Test
    public void testNewRemoteSFTPWithProgress() throws Exception {
        ByteArraySourceFile file = new ByteArraySourceFile("test-sftp.txt", "hello world\n".getBytes(Charset.forName("UTF-8")));
        try (RetryableSchmizzSSHClientFactory ssh = new RetryableSchmizzSSHClientFactory(endpoint, password, publicKeyDigest)) {
            ssh.upload(file, file.getName(), new LoggingProgressObserver());
        }
    }
    
    public static class LoggingProgressObserver implements Observer<Progress> {

        @Override
        public void observe(Progress progress) {
            log.debug("Upload progress: {} max: {}", progress.getCurrent(), progress.getMax());
        }
        
    }

}
