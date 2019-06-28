/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class ProviderUtil {

//    private final static Logger log = LoggerFactory.getLogger(ProviderUtil.class);
    private static final LogUtil.Logger log = new LogUtil.Logger();

    public static File getJavaSecurityFile() {
        File javaSecurityFile = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "java.security");
        return javaSecurityFile;
    }

    /**
     * Assumes that security.provider.n starts with 1 and that there are no
     * gaps. Reference:
     * http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html
     *
     * @return
     * @throws IOException
     */
    public static List<String> readSecurityProviderList() throws IOException {
        ArrayList<String> providerList = new ArrayList<>();
        File javaSecurityFile = getJavaSecurityFile();
        try (InputStream in = new FileInputStream(javaSecurityFile)) {
            Properties properties = new Properties();
            properties.load(in);
            int index = 1;
            while (true) {
                String providerName = properties.getProperty(String.format("security.provider.%d", index));
                if (providerName == null) {
                    break;
                }
                providerList.add(providerName);
                index++;
            }
        }
        return providerList;
    }

    /**
     * Assumes that security.provider.n starts with 1 and that there are no
     * gaps. Reference:
     * http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html
     *
     * If the specified provider is not already listed, appends a line to the
     * end of the file with this provider at the next available index number.
     *
     * @param providerName fully qualified class name of the provider to add to
     * the list
     * @throws IOException
     */
    public static void appendSecurityProvider(String providerName) throws IOException {
        List<String> providerList = readSecurityProviderList();
        if (providerList.contains(providerName)) {
            log.debug("Provider already in list: {}", providerName);
            return;
        }
        int next = providerList.size() + 1;
        File javaSecurityFile = getJavaSecurityFile();
        try (OutputStream out = new FileOutputStream(javaSecurityFile, true)) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
            writer.println(String.format("security.provider.%d=%s", next, providerName));
        }
    }
}
