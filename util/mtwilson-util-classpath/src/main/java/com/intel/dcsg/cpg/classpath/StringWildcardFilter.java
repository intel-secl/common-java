/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.classpath;

import com.intel.mtwilson.pipe.Filter;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * Handles 4 kinds of matches:
 *
 * equal to: "foo.bar.Xyz"
 *
 * starts with: "foo.bar.*" would match any class in foo.bar or sub-packages
 *
 * ends with: "*Impl" would match "foo.bar.XyzImpl"
 *
 * contains: "*.impl.*" would match anything in the foo.bar.impl package or its sub-packages.
 *
 * The wildcards must be either the first or last character of the matching string or they will not be recognized.
 *
 * @author jbuhacoff
 */
public class StringWildcardFilter implements Filter<String> {

    private HashSet<String> contains = new HashSet<String>();
    private HashSet<String> startsWith = new HashSet<String>();
    private HashSet<String> endsWith = new HashSet<String>();
    private HashSet<String> equalTo = new HashSet<String>();

    /**
     * You must call add(String) or addAll(Collection) to add criteria before using the filter or
     * else it will reject everything.
     * 
     */
    public StringWildcardFilter() {
    }
    public StringWildcardFilter(Collection<String> matchPatterns) {
        addAll(matchPatterns);
    }
    public StringWildcardFilter(String... matchPatterns) {
        addAll(matchPatterns);
    }
    
    public final void add(String pattern) {
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            contains.add(pattern.substring(1, pattern.length() - 1)); // strip off the wildcards
        } else if (pattern.startsWith("*")) {
            endsWith.add(pattern.substring(1));
        } else if (pattern.endsWith("*")) {
            startsWith.add(pattern.substring(0, pattern.length() - 1));
        } else {
            equalTo.add(pattern);
        }        
    }
    
    public final void addAll(Collection<String> patterns) {
        for (String pattern : patterns) {
            add(pattern);
        }        
    }

    public final void addAll(String... patterns) {
        for (String pattern : patterns) {
            add(pattern);
        }        
    }
    
    @Override
    public boolean accept(String item) {
        for (String pattern : equalTo) {
            if (item.equals(pattern)) {
                return true;
            }
        }
        for (String pattern : startsWith) {
            if (item.startsWith(pattern)) {
                return true;
            }
        }
        for (String pattern : endsWith) {
            if (item.endsWith(pattern)) {
                return true;
            }
        }
        for (String pattern : contains) {
            if (item.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
