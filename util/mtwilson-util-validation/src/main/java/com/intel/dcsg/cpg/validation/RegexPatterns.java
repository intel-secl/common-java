/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.dcsg.cpg.validation;

/**
 * XXX TODO the networking patterns should probably move to cpg-net 
 * @author jbuhacoff and ssbangal
 */
public class RegexPatterns {
    public static final String ALPHANUMERIC = "(?:[a-zA-Z0-9]+)"; 
    public static final String HEX = "(?:[0-9a-fA-F]+)"; 
    public static final String HEX_WITH_SEPARATOR = "(?:(?:[0-9a-fA-F]{2}[^0-9a-fA-F])*[0-9a-fA-F]{2})"; 
    public static final String HEX_WITH_OPTIONAL_SEPARATOR = "(?:(?:[0-9a-fA-F]{2}[^0-9a-fA-F]?)*[0-9a-fA-F]{2})"; 
    public static final String DEFAULT = "(?:[a-zA-Z0-9\\[\\]$@(){}_\\.\\, |:-]+)"; // should not include quotes, blackslashes, or angle brackets < > ;  note that it DOES include $ and @ which would not be safe in the shell or some scripting languages, but are not a problem at all for database and javascript/html  (and always show up in java default toString() output)
    public static final String IPADDRESS = "(?:(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))";
    public static final String FQDN = "(?:(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]))"; // TODO: rename this as RFC 952 FQDN which requires it to start with a letter;  make another one RFC 1123 FQDN which allows it to start with a digit too and use that one for hostname checking.
    public static final String IPADDR_FQDN = "(?:" + IPADDRESS + "|" + FQDN + ")";
    public static final String EMAIL = "(?:([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6}))";
    public static final String PASSWORD = "(?:([a-zA-Z0-9_\\.\\, @!#$%^&+=()\\[\\]\"'*-]+))"; 
    public static final String PORT = "(?:([0-9]{1,5}))";
    public static final String UUID = "(?:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})";
    public static final String ANY_VALUE = "((?s).*)"; //"(?:.*)";
}
