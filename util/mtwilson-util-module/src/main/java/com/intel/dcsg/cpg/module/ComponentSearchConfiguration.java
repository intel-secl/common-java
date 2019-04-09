/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 * 
 * @author jbuhacoff
 */
public class ComponentSearchConfiguration {
    private String[] names = null;
    private boolean annotations = true;
    private boolean conventions = true;

    public ComponentSearchConfiguration withAnnotations() { annotations = true; return this; }
    public ComponentSearchConfiguration noAnnotations() { annotations = false; return this; }
    public ComponentSearchConfiguration withConventions() { conventions = true; return this; }
    public ComponentSearchConfiguration noConventions() { conventions = false; return this; }
    public ComponentSearchConfiguration withNames(String[] classNames) { names = classNames; return this; }
    public ComponentSearchConfiguration noNames() { names = null; return this; }
    public String[] names() { return names; }
    public boolean annotations() { return annotations; }
    public boolean conventions() { return conventions; }
    
}
