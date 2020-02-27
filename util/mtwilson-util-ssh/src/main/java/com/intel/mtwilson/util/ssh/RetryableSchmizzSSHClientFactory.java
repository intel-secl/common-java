/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.performance.Observer;
import com.intel.dcsg.cpg.performance.Progress;
import com.intel.mtwilson.retry.Backoff;
import com.intel.mtwilson.retry.ConstantBackoff;
import com.intel.mtwilson.retry.Retry;
import com.intel.mtwilson.retry.Retryable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.TransferListener;

/**
 *
 * @author jbuhacoff
 */
public class RetryableSchmizzSSHClientFactory implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryableSchmizzSSHClientFactory.class);
    private Endpoint endpoint;
    private Password password;
    private String publicKeyDigest;
    private long connectionTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private long readInterval = TimeUnit.MILLISECONDS.convert(100, TimeUnit.MILLISECONDS);
    private Timeout timeout;
    private Backoff backoff = new ConstantBackoff(5000);
    private SSHClient client;

    public RetryableSchmizzSSHClientFactory(Endpoint endpoint, Password password, String publicKeyDigest) {
        this(endpoint, password, publicKeyDigest, new Timeout(1, TimeUnit.MINUTES));
    }
    public RetryableSchmizzSSHClientFactory(Endpoint endpoint, Password password, String publicKeyDigest, Timeout timeout) {
        this.endpoint = endpoint;
        this.password = password;
        this.publicKeyDigest = publicKeyDigest;
        this.timeout = timeout;
    }
    
    @Override
    public void close() throws IOException {
        if (client != null && client.isConnected()) {
            client.close();
        }
    }

    // just move to the RetryableSSHConnection?  or rewrite all the other code to just use this directly with tight binding?
    public Exit execute(String command, Output output) throws Exception {
        RetryableCommand retryable = new RetryableCommand(this, command, output);
        return Retry.limited(retryable, 5, backoff);
    }

    public void upload(File source, String remotePath) throws Exception {
        RetryableFileTransfer retryable = new RetryableFileTransfer(this, source, remotePath);
        if (!Retry.limited(retryable, 5, backoff)) {
            throw new Exception("upload failed");
        }
    }

    public void upload(File source, String remotePath, Observer<Progress> listener) throws Exception {
        // make progress object, pass it to this,  then 
        RetryableFileTransfer retryable = new RetryableFileTransfer(this, source, remotePath, listener);
        if (!Retry.limited(retryable, 5, backoff)) {
            throw new Exception("upload failed");
        }
    }

    public void upload(LocalSourceFile source, String remotePath, Observer<Progress> listener) throws Exception {
        // make progress object, pass it to this,  then 
        RetryableFileTransfer retryable = new RetryableFileTransfer(this, source, remotePath, listener);
        if (!Retry.limited(retryable, 5, backoff)) {
            throw new Exception("upload failed");
        }
    }
    
    public SSHClient connect() throws Exception {
        if (client != null && client.isConnected()) {
            return client;
        }
        RetryableConnection retryable = new RetryableConnection(this);
        client = Retry.limited(retryable, 5, backoff);
        return client;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getReadInterval() {
        return readInterval;
    }

    public long getTimeout() {
        return timeout.getMillis();
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReadInterval(long readInterval) {
        this.readInterval = readInterval;
    }

    public void setTimeout(long timeout) {
        this.timeout = new Timeout(timeout, TimeUnit.MILLISECONDS);
    }

    public Backoff getBackoff() {
        return backoff;
    }

    public void setBackoff(Backoff backoff) {
        this.backoff = backoff;
    }

    public static class FileTransfer implements Progress {

        private SFTPClient sftpClient;
        private LocalSourceFile sourceFile;
        private String remotePath;
        private long progress, progressMax;

        public FileTransfer(SFTPClient sftpClient, LocalSourceFile localSourceFile, String remotePath) {
            this.sftpClient = sftpClient;
            this.sourceFile = localSourceFile;
            this.remotePath = remotePath;
            this.progress = 0;
            this.progressMax = localSourceFile.getLength();
        }

        public SFTPClient getSFTPClient() {
            return sftpClient;
        }

        public LocalSourceFile getLocalSourceFile() {
            return sourceFile;
        }

        public String getRemotePath() {
            return remotePath;
        }

        @Override
        public long getCurrent() {
            return progress;
        }

        @Override
        public long getMax() {
            return progressMax;
        }
    }

    public static class RetryableConnection implements Retryable<SSHClient> {

        private RetryableSchmizzSSHClientFactory factory;

        public RetryableConnection(RetryableSchmizzSSHClientFactory factory) {
            this.factory = factory;
        }

        @Override
        public SSHClient call() throws IOException {
            try {
                SSHClient client = new SSHClient();
                RemoteHostKeyDigestVerifier hostKeyVerifier = new RemoteHostKeyDigestVerifier(factory.endpoint.getHost(), factory.endpoint.getPort(), "MD5", factory.publicKeyDigest); // md5 is insecure but this is done for compatibility with command-line ssh clients that show md5 hash of remote host public key for user to verify
                client.addHostKeyVerifier(hostKeyVerifier);
                client.setConnectTimeout((int) factory.connectionTimeout);
                client.connect(factory.endpoint.getHost(), factory.endpoint.getPort());
                client.authPassword(factory.endpoint.getUsername(), factory.password.toCharArray());
                return client;
            } catch (IOException e) {
                log.error("Connect to {} failed: {}", factory.endpoint.getHost(), e.getMessage());
                throw e;
            }
        }

        @Override
        public boolean isRetryable(Exception e) {
            if (e instanceof UserAuthException) {
                return false;
            }
            return true;
        }

        @Override
        public void close() throws IOException {
            factory.close();
        }
    }

    public static class RetryableCommand implements Retryable<Exit> {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryableCommand.class);
        private RetryableSchmizzSSHClientFactory factory;
        private SSHClient client;
        private String command;
        private Output output;

        public RetryableCommand(RetryableSchmizzSSHClientFactory factory, String command, Output output) {
            this.factory = factory;
            this.command = command;
            this.output = output;
        }

        private void open() throws Exception {
            if( client == null || !client.isConnected() ) {
                log.debug("Connecting to {}", factory.getEndpoint().getHost());
                client = factory.connect();
            }
        }
        
        @Override
        public Exit call() throws Exception { // ConnectionException, TransportException, IOException
            open();
            log.debug("executing command: {}", command);
            try (Session session = client.startSession()) {
                log.debug("calling schmizz exec for command: {}", command);
                Session.Command sshresult = session.exec(command); // throws ConnectionException, TransportException
                InputStream stdout = sshresult.getInputStream();
                InputStream stderr = sshresult.getErrorStream();
                int stdoutAvailable = stdout.available();
                int stderrAvailable = stderr.available();
                int stdoutReceived = 0, stderrReceived = 0;
                try (OutputStream stdoutLog = output.getOutputStream(); OutputStream stderrLog = output.getErrorStream()) {
                    AlarmClock delay = new AlarmClock(factory.readInterval, TimeUnit.MILLISECONDS);
                    //while ( stdoutAvailable > 0 || stdoutReceived > -1 || stderrAvailable > 0 || stderrReceived > -1) {
                    log.debug("waiting to receive data...");
                    while (stdoutReceived > -1 || stderrReceived > -1) {
                        log.debug("stdout avail: {} recvd: {}   stderr avail: {} recvd: {}", stdoutAvailable, stdoutReceived, stderrAvailable, stderrReceived);
                        stdoutAvailable = stdout.available();
                        stderrAvailable = stderr.available();
//                if (stdoutAvailable > 0) {
                        log.debug("reading stdout: {}", stdoutAvailable);
                        byte[] stdoutBuffer = new byte[stdoutAvailable];
                        stdoutReceived = stdout.read(stdoutBuffer, 0, stdoutAvailable);
                        if (stdoutReceived > -1) {
                            log.debug("writing stdout log: {}", stdoutReceived);
                            stdoutLog.write(stdoutBuffer, 0, stdoutReceived);
                        }
//                }
//                if (stderrAvailable > 0) {
                        log.debug("reading stderr: {}", stderrAvailable);
                        byte[] stderrBuffer = new byte[stderrAvailable];
                        stderrReceived = stderr.read(stderrBuffer, 0, stderrAvailable);
                        if (stderrReceived > -1) {
                            log.debug("writing stderr log: {}", stderrReceived);
                            stderrLog.write(stderrBuffer, 0, stderrReceived);
                        }
//                }
                        delay.sleep();
                    }
                }
                log.debug("waiting with timeout for command to finish");
                sshresult.join((int) factory.timeout.getMillis(), TimeUnit.MILLISECONDS);

                /*
                 String stdoutText = IOUtils.toString(stdout, "UTF-8"); // throws IOException
                 String stderrText = IOUtils.toString(stderr, "UTF-8"); // throws IOException
                 log.debug("result: {}", stdoutText);

                 // log the output
                 // store the stdout into a file
                 File stdoutFile = new File(taskDirectoryPath + File.separator + String.valueOf(commandId) + ".stdout.log");
                 FileUtils.writeStringToFile(stdoutFile, stdoutText, Charset.forName("UTF-8"));

                 // store the stderr into a file
                 File stderrFile = new File(taskDirectoryPath + File.separator + String.valueOf(commandId) + ".stderr.log");
                 FileUtils.writeStringToFile(stderrFile, stderrText, Charset.forName("UTF-8"));
                 */

                return new Exit(sshresult.getExitStatus(), sshresult.getExitErrorMessage(), sshresult.getExitSignal());
            }
        }

        @Override
        public void close() throws IOException {
            if (client != null) {
                client.close();
            }
        }

        @Override
        public boolean isRetryable(Exception e) {
            if (e instanceof IOException) {
                return true;
            }
            return false;
        }
    }

    public static class RetryableFileTransfer implements Retryable<Boolean> {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryableFileTransfer.class);
        private RetryableSchmizzSSHClientFactory factory;
        private SSHClient sshClient;
        private SFTPClient sftpClient;
        private String remotePath;
        private LocalSourceFile sftpSourceFile;
        private final Observer<Progress> listener;

        public RetryableFileTransfer(RetryableSchmizzSSHClientFactory factory, File source, String remotePath) {
            this(factory, source, remotePath, null);
        }

        public RetryableFileTransfer(RetryableSchmizzSSHClientFactory factory, File source, String remotePath, Observer<Progress> listener) {
            this(factory, new FileSystemFile(source), remotePath, listener);
        }

        public RetryableFileTransfer(RetryableSchmizzSSHClientFactory factory, String sourceName, byte[] sourceBytes, String remotePath, Observer<Progress> listener) {
            this(factory, new ByteArraySourceFile(sourceName, sourceBytes), remotePath, listener);
        }
        
        public RetryableFileTransfer(RetryableSchmizzSSHClientFactory factory, LocalSourceFile source, String remotePath, Observer<Progress> listener) {
            this.factory = factory;
            this.remotePath = remotePath;
            this.sftpSourceFile = source;
            this.listener = listener;
        }
        
        
        @Override
        public boolean isRetryable(Exception e) {
            if (e instanceof IOException) {
                return true;
            }
            return false;
        }
        
        private void open() throws Exception {
            if( sshClient == null || !sshClient.isConnected() ) {
                sshClient = factory.connect();
            }
            if (sftpClient == null) {
                sftpClient = sshClient.newSFTPClient();
            }            
        }

        @Override
        public Boolean call() throws Exception {
            open();
            SFTPFileTransfer transfer = sftpClient.getFileTransfer();
            SFTPTransferProgress transferProgress = new SFTPTransferProgress(listener);
            transfer.setTransferListener(transferProgress);
            transfer.upload(sftpSourceFile, remotePath);
            return true;
        }

        @Override
        public void close() throws IOException {
            if (sftpClient != null) {
                sftpClient.close();
            }
        }
    }

    /**
     * A bridge between our Task which implements Progress as well as the
     * StreamCopier.Listener interface, and the TransferListener interface that
     * SFTPClient expects.
     */
    public static class SFTPTransferProgress implements TransferListener, Progress {

        private final String relativePath;
        private final Observer<Progress> listener;
        private long progress, progressMax;

        public SFTPTransferProgress(String relativePath) {
            this.relativePath = relativePath;
            this.listener = null;
        }

        public SFTPTransferProgress(String relativePath, Observer<Progress> listener) {
            this.relativePath = relativePath;
            this.listener = listener;
        }

        public SFTPTransferProgress(Observer<Progress> listener) {
            this.relativePath = "";
            this.listener = listener;
        }
        
        @Override
        public long getCurrent() {
            return progress;
        }

        @Override
        public long getMax() {
            return progressMax;
        }

        @Override
        public TransferListener directory(String name) {
            log.debug("Started transferring directory: {}", name);
            return new SFTPTransferProgress(relativePath + name + "/", listener);
        }

        @Override
        public StreamCopier.Listener file(String name, long size) {
            progress = 0;
            progressMax = size;
            String path = relativePath + name;
            log.debug("Started transferring file: {} size: {}", path, size);
            if( listener != null ) {
                listener.observe(this);
            }
            return new StreamCopier.Listener() {
                /**
                 * Listens for progress updates from SFTPClient and updates our
                 * file transfer status by setting current (completed) to number
                 * of bytes transferred.
                 *
                 */
                @Override
                public void reportProgress(long bytesTransferred) throws IOException {
                    progress = bytesTransferred;
                    if (listener != null) {
                        listener.observe(SFTPTransferProgress.this);
                    }
                }
            };

        }
    }


}
