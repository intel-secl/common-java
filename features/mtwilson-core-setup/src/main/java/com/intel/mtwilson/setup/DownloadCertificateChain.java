package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.jaxrs2.client.CMSClient;
import com.intel.mtwilson.shiro.ShiroUtil;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Properties;

public class DownloadCertificateChain extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DownloadCertificateChain.class);
    private static final String CMS_BASE_URL = "cms.base.url";
    private final String trustedCAFile = Folders.configuration() + "/trustedca/root-ca-cert.pem";

    @Override
    protected void configure() {
        if (getConfiguration().get(CMS_BASE_URL) == null || getConfiguration().get(CMS_BASE_URL).isEmpty()) {
            configuration("CMS Base Url is not provided");
        }
    }

    @Override
    protected void validate() {
        ShiroUtil shiroUtil = new ShiroUtil();
        Certificate caCertX509 = shiroUtil.readCertificateFromFile(trustedCAFile);
        if (caCertX509 == null) {
            validation("Error reading CA certificate from file: {}", trustedCAFile);
        } else {
            log.info("CA certificate successfully downloaded from CMS");
        }
    }

    @Override
    protected void execute() {
        log.info("Downloading CA certificate from CMS...");
        Certificate caCertX509;
        try {
            CMSClient cmsClient = new CMSClient(new Properties(),
                    new TlsConnection(new URL(getConfiguration().get(CMS_BASE_URL)), TlsPolicyBuilder.factory().insecure().build()));
            caCertX509 = cmsClient.getCACertificate();
            PemWriter writer = new PemWriter(new FileWriter(trustedCAFile));
            writer.writeObject(new PemObject("CERTIFICATE", caCertX509.getEncoded()));
            writer.close();
        } catch (MalformedURLException exc) {
            log.error("Incorrect CMS URL provided in env");
        } catch (IOException exc) {
            log.error("Error writing CA certificate to file");
        }catch (Exception exc) {
            log.error("Error creating CMS client: {}", exc.getMessage());
        }
    }
}
