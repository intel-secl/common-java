/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.net;

import com.intel.dcsg.cpg.validation.ObjectModel;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports standard decimal notation for IPv4 such as 192.168.0.1
 * 
 * This class represents the address for a single host. 
 * Intentionally does not support subnet/CIDR notation. That belongs in a NetworkAddress class.
 * 
 * @author jbuhacoff
 */
public class IPv4Address extends ObjectModel {
    private static final String rDecimalByte = "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";
    public static final String IPv4_REGEXP = "(?:"+rDecimalByte+"\\.){3}"+rDecimalByte;
    private static final Pattern pIPv4 = Pattern.compile(IPv4_REGEXP);
    
    private String input;
    
    public IPv4Address(String text) {
        input = text.trim();
    }
    
    @Override
    protected void validate() {
        Matcher mIPv4 = pIPv4.matcher(input);
        if( mIPv4.matches() ) {
            return;
        }
        fault("Unrecognized IPv4 format: %s", input);
    }
    
    @Override
    public String toString() { return input; }
    
    public byte[] toByteArray() { 
        if( !isValid() ) { return null; }
        try {
            return Inet4Address.getByName(input).getAddress(); // XXX is this ok or is there another way?
        }
        catch(UnknownHostException e) {
            return null;
        }
    }
    
    public static boolean isValid(String address) {
        return pIPv4.matcher(address).matches();
    }
    
}
