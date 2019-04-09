/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.classpath;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarFile;

/**
 * The MultiJarFileClassLoader can load classes from a set of jar files. It behaves just like the JarFileClassLoader but
 * with multiple jars instead of one jar.
 *
 * TODO  should rename it to LimitedMultiJarFileClassLoader  to indicate it looks down first and then up
 * (opposite order from URLClassLoader)
 *
 * @author jbuhacoff
 */
public class MultiJarFileClassLoader extends LimitedClassLoader implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MultiJarFileClassLoader.class);
    private final ArrayList<ClasspathEntry> classpath = new ArrayList<>();

    /**
     * Create a MultiJarFileClassLoader with an empty classpath; you need to call add(File[] classpath) in order to have
     * a usable classloader.
     *
     * @throws IOException
     */
    public MultiJarFileClassLoader() {
        super();
    }

    /**
     * Create a MultiJarFileClassLoader with an empty classpath; you need to call add(File[] classpath) in order to have
     * a usable classloader.
     *
     * @param parent
     * @throws IOException
     */
    public MultiJarFileClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Create a MultiJarFileClassLoader with the given classpath. You can still call add(File[] classpath) later to add
     * more files to the classpath.
     *
     * @param classpath
     * @throws IOException
     */
    public MultiJarFileClassLoader(File[] classpath) throws IOException {
        super();
        add(classpath);
    }

    /**
     * Create a MultiJarFileClassLoader with the given classpath. You can still call add(File[] classpath) later to add
     * more files to the classpath.
     *
     * @param classpath
     * @param parent
     * @throws IOException
     */
    public MultiJarFileClassLoader(File[] classpath, ClassLoader parent) throws IOException {
        super(parent);
        add(classpath);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        log.debug("findClass {}", name);
        try {
                for (ClasspathEntry entry : classpath) {
                    byte[] data = JarUtil.readClass(entry.jarFile, name);
                    if (data == null) {
                        continue;
                    }
                    Class<?> local = defineClass(name, data, 0, data.length);
                    log.debug("Loaded class {} from file {}", name, entry.file.getName());
                    return local;
                }
            log.debug("Cannot find class {}", name);
            return null;
        } catch (Exception e) {
            log.debug("Cannot load class {}", name, e);
            throw new ClassNotFoundException("Cannot load class: " + name, e);
        }
    }

    /**
     * You can add jars to the classpath. Jar files are scanned for classes in the order they appear on the classpath.
     *
     * @param classpath
     */
    public final void add(File[] classpath) throws IOException {
        for (File file : classpath) {
            add(file);
        }
    }

    public final void add(Collection<File> classpath) throws IOException {
        for (File file : classpath) {
            add(file);
        }
    }

    public final void add(File file) throws IOException {
        if( !file.exists() ) {
            log.warn("Cannot add missing file to classpath: {}", file.getAbsolutePath());
            return;
        }
        if( file.isDirectory() ) {
            log.warn("Cannot add directory to classpath: {}", file.getAbsolutePath());
            return;
        }
        if( !file.isFile() ) {
            log.warn("Cannot add non-file to classpath: {}", file.getAbsolutePath());
            return;
        }
        this.classpath.add(new ClasspathEntry(file, new JarFile(file)));
    }

    @Override
    public void close() throws IOException {
        for (ClasspathEntry entry : classpath) {
            entry.jarFile.close();
        }
    }

    private static class ClasspathEntry {

        File file;
        JarFile jarFile;

        ClasspathEntry(File file, JarFile jarFile) {
            this.file = file;
            this.jarFile = jarFile;
        }
    }
}
