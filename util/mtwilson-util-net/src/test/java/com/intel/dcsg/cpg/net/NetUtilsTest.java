/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.net;

import java.net.SocketException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class NetUtilsTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetUtilsTest.class);

    @Test
    public void testGetLocalAddresses() throws SocketException {
        List<String> addresses = NetUtils.getNetworkAddressList();
        log.debug("addresses: {}", addresses);
    }
    @Test
    public void testGetLocalHostnames() throws SocketException {
        List<String> hostnames = NetUtils.getNetworkHostnameList();
        log.debug("hostnames: {}", hostnames);
    }

}
