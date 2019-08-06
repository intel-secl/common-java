/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.data.bundle;

import java.util.Iterator;

/**
 * Represents a feature that contributes files to the bundle for export.
 * 
 * @author jbuhacoff
 */
public interface Contributor {
    Iterator<Entry> contribute();
    void receive(Bundle archive);
}
