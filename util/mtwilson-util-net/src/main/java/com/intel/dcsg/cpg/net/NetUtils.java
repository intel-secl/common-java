/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.net;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class NetUtils {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetUtils.class);

    // TODO: remove from TrustagentConfiguration and catch the SocketException to return empty list
    public static List<String> getNetworkAddressList() throws SocketException {
        ArrayList<String> ipList = new ArrayList<>();
        List<NetworkInterface> nets = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface netint : nets) {
            List<InetAddress> inetAddresses = Collections.list(netint.getInetAddresses());
            for (InetAddress inetAddress : inetAddresses) {
                if (inetAddress.getHostAddress() != null && !inetAddress.getHostAddress().isEmpty())
                    ipList.add(inetAddress.getHostAddress());
            }
        }
        return ipList;
    }
    
    
    public static List<String> getIPv4NetworkAddressList() throws SocketException {
        ArrayList<String> ipList = new ArrayList<>();
        List<NetworkInterface> nets = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface netint : nets) {
            List<InetAddress> inetAddresses = Collections.list(netint.getInetAddresses());
            for (InetAddress inetAddress : inetAddresses) {
                String address = inetAddress.getHostAddress();
                if (address != null && !address.isEmpty() && IPv4Address.isValid(address)) {
                    ipList.add(address);
                }
            }
        }
        return ipList;
    }
    
    // TODO: remove from TrustagentConfiguration and catch the SocketException to return empty list
    public static List<String> getNetworkHostnameList() throws SocketException {
        ArrayList<String> dnList = new ArrayList<>();
        List<NetworkInterface> nets = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface netint : nets) {
            List<InetAddress> inetAddresses = Collections.list(netint.getInetAddresses());
            for (InetAddress inetAddress : inetAddresses) {
                if (inetAddress.getHostName() != null && !inetAddress.getHostName().isEmpty() && !inetAddress.getHostName().equals(inetAddress.getHostAddress()))
                    dnList.add(inetAddress.getHostName());
            }
        }
        return dnList;
    }
    
    
}
