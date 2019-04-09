/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 *
 * @author jbuhacoff
 */
public interface ClassLoadingStrategy {
    ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException;
}
