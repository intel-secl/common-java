/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.io.file;

import java.io.File;
import java.io.FileFilter;

/**
 * An implementation of {@code java.io.FileFilter} that only accepts files
 * (not directories).
 */
public class FileOnlyFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.isFile();
    }
    
}
