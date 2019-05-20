/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.dcsg.cpg.net;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Model;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a hostname. This class enforces some rules on the 
 * syntax of the hostname to make it usable without further type checking.
 * 
 * A Hostname can also contain an IP address value, even though the model
 * objects are not currently related in any way. 
 * 
 * TODO relate the Hostname and IPAddress models in some way, possibly with
 * an aggregated type or union, something that can be a HostnameOrIpAddress.
 * See InternetAddress
 * 
 * XXX TODO need to rewrite as extension to ObjectModel
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class Hostname implements Model {

    private String hostname = null;

    public Hostname(String hostname) {
        setHostname(hostname);
    }


    public final void setHostname(String hostname) {
        if( hostname == null ) { throw new IllegalArgumentException("Missing hostname"); } // or NullPointerException
        if( hostname.isEmpty() ) { throw new IllegalArgumentException("Hostname is empty"); } // or IllegalArgumentException
        if (isValid(hostname)) {
            this.hostname = hostname;
        } else {
            throw new IllegalArgumentException("Invalid hostname: " + hostname);
        }
    }

    /**
     * Returns the hostname so that you can easily concatenate to a string.
     * Example: assert new Hostname("1.2.3.4").toString().equals("1.2.3.4");
     *
     * @see java.lang.Object#toString()
     */
    @com.fasterxml.jackson.annotation.JsonValue
    @Override
    public String toString() {
        return hostname;
    }

    // XXX TODO need to extend ObjectModel so we get this for free...
    @Override
    public boolean isValid() {
        return isValid(hostname);
    }

    /**
     * This method does NOT check the network for the existence of the given
     * hostname, it only checks its format for validity and, if an IPv4 or IPv6
     * hostname is given, checks that it is within the allowed range.
     *
     * @param hostname to check for validity, such as 1.2.3.4
     * @return true if the hostname appears to be a valid IPv4 or IPv6 address,
     * false if the hostname is null or otherwise invalid
     */
    public static boolean isValid(String hostname) {
        return ValidationUtil.isValidWithRegex(hostname, RegexPatterns.FQDN);
    }
    
    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Hostname other = (Hostname) obj;
        if ((this.hostname == null) ? (other.hostname != null) : !this.hostname.equals(other.hostname)) {
            return false;
        }
        return true;
    }

    @Override
    public List<Fault> getFaults() {
        if( isValid() ) {
            return Collections.EMPTY_LIST;
        }
        else {
            ArrayList<Fault> faults = new ArrayList<Fault>();
            faults.add(new Fault("Invalid hostname: %s", hostname));            
            return faults;
        }
    }
}
