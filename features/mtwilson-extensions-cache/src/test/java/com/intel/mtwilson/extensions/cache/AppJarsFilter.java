/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.extensions.cache;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author jbuhacoff
 */
public class AppJarsFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().contains("mtwilson");
    }
    
}
