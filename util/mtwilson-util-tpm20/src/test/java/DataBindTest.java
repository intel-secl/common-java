/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.util.tpm20.DataBind;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rksavino
 */
public class DataBindTest {

    public DataBindTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void bindUnbind() throws NoSuchAlgorithmException, GeneralSecurityException, Exception {
        String tlsDistinguishedName = "CN=data-bind-test,OU=mtwilson";
        KeyPair testKey = RsaUtil.generateRsaKeyPair(2048);

        X509Builder builder = X509Builder.factory();
        builder.selfSigned(tlsDistinguishedName, testKey);
//        builder.issuerName(cacert);
//        builder.issuerPrivateKey(cakey);
        builder.subjectName(tlsDistinguishedName);
        builder.subjectPublicKey(testKey.getPublic());
        X509Certificate testCert = builder.build();

        if (testCert == null) {
            List<Fault> faults = builder.getFaults();
            for (Fault fault : faults) {
                System.out.println(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
            }
            throw new Exception("Cannot generate TLS certificate");
        }

        PublicKey testPubKey = testCert.getPublicKey();

        byte[] encrypted = DataBind.bind("test".getBytes(), testPubKey);
        System.out.println(String.format("Encrypted string: %s", Base64.encodeBase64String(encrypted)));

        Provider bc = new BouncyCastleProvider();
        Cipher cipher = Cipher.getInstance("RSA", bc);
        PSource pSource = new PSource.PSpecified(new byte[]{'T', 'P', 'M', '2', 0});
        OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, pSource);
        cipher.init(Cipher.DECRYPT_MODE, testKey.getPrivate(), oaepParameterSpec); // throws InvalidKeyException, InvalidAlgorithmParameterException
        byte[] original = cipher.doFinal(encrypted);
        System.out.println(String.format("Decrypted string: %s", new String(original)));
    }
    
    @Test
    public void bindWithProvidedTpmKey() throws CertificateException, GeneralSecurityException {
        String bindingKeyPem = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEoDCCA4igAwIBAgIIKM1Sg7pFRj0wDQYJKoZIhvcNAQELBQAwGzEZMBcGA1UEAxMQbXR3aWxzb24tcGNhLWFpazAeFw0xOTAxMTAwOTA2MjdaFw0yOTAxMDcwOTA2MjdaMCUxIzAhBgNVBAMMGkNOPUJpbmRpbmdfS2V5X0NlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuXrOMeVbvpXiGT0EsMeu7VJ9R/H5qxIdRr3prMfZW983wacqNbICZlOtqoZj1q06cuXY4bGZg0B76WqMsOCwzN15wB2p4UNDMtpBjs1DQsDLlLimvPs+d3KtjBqdAKX2bkdO0/H9j14KxCy3YWztvf9XcO6M55HND00YO6RCwomKBgXyCoz85Smzj5DywVBHGzQooC8hh0jMaNShWPhgoTWrbiO5dt4AxN7kZh7IuNAAyL6jOZcJ2o0C1j/ugPWCRG7HAZDVAa6pAeB9owREBUUFr4Nnp8AaH1ynSUwG9npS4aCG2a84fBpOIAzo7gXkgp+o3C9jLWBhWLJjjzvbBQIDAQABo4IB3DCCAdgwDgYDVR0PAQH/BAQDAgUgMIGdBgdVBIEFAwIpBIGR/1RDR4AXACIAC+YgIk9bfElA8fA4xEZSVZh8rAlBIWkN4u0cSXfFcWUEAAQA/1WqAAAAAAXDupAAAAAHAAAAAQEABwA+AAw2AAAiAAs/FZ3yCkvkM/FR6jCeIpsVQ7pWdVc15x1pHNvHqdq2fwAiAAsiuSnka4nbg6zS7LgW6DhmC60WKgm7ilwdjQ0Kfs/rBzCCARQGCFUEgQUDAikBBIIBBgAUAAsBAHRhqg7RWRIvpa6I69B4qnz2OaHwpc8kbydWI+EHfJ4p0liJzmX7rLbx/lPlbOVk6PpHxNGaOgsKqOLAHv46QHGCyAk4EUxYeW8SAHXf5CFG2zeZ/Fw/XRG2/IVwfgmf8x8J89B3l6boukzKn8lMsivsW1IGUjn1ANusDD/IvKdkUvnCnDAgG2dNSS3xdnkFQL7zojVd/S+IVunWReHiTar99LtfgwTVdYKONsnq/1oA6lxq3o3o9QskL1dEjPnaobQPbzHS8w2vP15MAbgswc9OH7purGRhGLIJ3bYE89+WcrKfWjK36T2wZ1USil1tQxkKvObL6+3NjgM8v/nu0MYwDgYIVQSBBQMCKQIEAgAAMA0GCSqGSIb3DQEBCwUAA4IBAQCyYc6iEPBeYi24sGyIBYglPQ71Smyuolkx0DaO9NYHq9hUbo+HMxpuVk90YGwhvi4KUST2MshdaYHNW3ciCn4MRgcU5NhTUKyPk1WeD/LvTlPOUj4BgMsaDbgtCPLSuVZ2TayHi3Rsiv66t6VmPUo0jl15qXr5iA43pO0wFDnclYYHYvEbu0u4C2BrGpe/ftBVnDz0viTBo0QQfPT0Y1J8XGK2EBudaAbOlZcnWag92/rb0TbAVCjn1L0MugKwDGyFDyK5PsJxRyeoSU5e+cCOeLIj2dTODVEIDykoQtYaDcwXVmOcHVC42UTnxxxaK1pypOFhGVE02X002XpUgA3i\n" +
                "-----END CERTIFICATE-----";
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(bindingKeyPem.getBytes(StandardCharsets.UTF_8));
        X509Certificate bindingKeyCert = (X509Certificate) fact.generateCertificate(is);
        PublicKey bindingPublicKey = bindingKeyCert.getPublicKey();
        
        System.out.println(String.format("Binding public key: %s", DatatypeConverter.printHexBinary(bindingKeyCert.getEncoded())));
                
        byte[] encrypted = DataBind.bind("test".getBytes(), bindingPublicKey);
        System.out.println(String.format("Encrypted string: %s", Base64.encodeBase64String(encrypted)));
        System.out.println(String.format("Encrypted hex: %s", DatatypeConverter.printHexBinary(encrypted)));
    }
}
