/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.data.bundle;

/**
 * Represents a single file in the bundle.
 * 
 * @author jbuhacoff
 */
public class Entry {
    private String path;
    private byte[] content;

    public Entry(String path, byte[] content) {
        this.path = path;
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }
    
}
