/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Bundle - a collection of things, or a quantity of material, tied or wrapped
 * up together.
 *
 * @author jbuhacoff
 */
public class ZipBundle {

    private final TreeMap<String, byte[]> contentMap;

    public ZipBundle() {
        this.contentMap = new TreeMap<>();
    }

    /**
     *
     * @param key
     * @param value must not be null
     */
    public void put(String key, byte[] value) {
        if (value == null) {
            throw new NullPointerException();
        }
        contentMap.put(key, value);
    }

    public byte[] get(String key) {
        return contentMap.get(key);
    }

    public void remove(String key) {
        contentMap.remove(key);
    }

    public Collection<String> index() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(contentMap.keySet());
        return Collections.unmodifiableList(list);
    }

    private static void addZipFileEntry(ZipOutputStream out, String filename, byte[] content) throws IOException {
        ZipEntry zipEntry = new ZipEntry(filename);
        out.putNextEntry(zipEntry);
        if (content != null) {
            out.write(content);
        }
        out.closeEntry();
    }

    private static byte[] readZipFileEntry(ZipInputStream in, ZipEntry zipEntry) throws IOException {
//        long size = zipEntry.getSize(); // uncompressed size of entry data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] content = new byte[1024]; // read up to 1KB at a time from the stream
        int readByteCount;
        while ((readByteCount = in.read(content)) != -1) {
            buffer.write(content, 0, readByteCount);
        }
        return buffer.toByteArray();
    }

    public void read(InputStream in) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry zipEntry = zip.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String key = zipEntry.getName(); //  examples: file1, directory/file2, directory/file3
                byte[] value = readZipFileEntry(zip, zipEntry);
                contentMap.put(key, value);
                zipEntry = zip.getNextEntry();
            }
        }
    }

    public void write(OutputStream out) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> entry : contentMap.entrySet()) {
                addZipFileEntry(zip, entry.getKey(), entry.getValue());
            }
        }
    }
}
