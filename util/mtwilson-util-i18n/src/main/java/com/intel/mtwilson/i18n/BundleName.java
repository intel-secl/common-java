/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.i18n;

/**
 * @author jbuhacoff
 */
public enum BundleName {
    MTWILSON_STRINGS("MtWilsonStrings"); // changed from mtwilson-strings to MtWilsonStrings because java convention is that resource bundles are equivalent to classes and should have the same naming convention
    
    private String bundleName;
    BundleName(String bundleName) {
        this.bundleName = bundleName;
    }
    
    public String bundle() {
        return bundleName;
    }
}
