/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.feature.cmd;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.io.file.DirectoryFilter;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.Folders;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Adds, updates, lists, and deletes features.
 *
 * @author jbuhacoff
 */
public class Feature extends AbstractCommand implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Feature.class);

    @Override
    public void execute(String[] args) throws Exception {
        log.debug("args length: {}", args.length);

        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: feature <add /path/to/feature.zip>|update /path/to/feature.zip|list|delete feature-id>");
        }
        String action = args[0];
        if (action.equals("add")) {
            add(args[1]);
        }
        if (action.equals("update")) {
            update(args[1]);
        }
        if (action.equals("delete")) {
            delete(args[1]);
        }
        if (action.equals("list")) {
            list();
        }
    }

    private void unzip(File zip, File toDirectory) throws IOException {
        if (!toDirectory.exists()) {
            toDirectory.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                Path targetPath = toDirectory.toPath().resolve(entry.getName());
                if (entry.isDirectory()) {
                    targetPath.toFile().mkdirs();
                } else {
                    byte[] content = IOUtils.toByteArray(zipIn);
                    Files.write(targetPath, content);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private com.intel.mtwilson.feature.xml.Feature readFeatureXmlFromZip(File zip) throws IOException, JAXBException, XMLStreamException {
        try (ZipFile zipfile = new ZipFile(zip)) {
            ZipEntry featureXmlEntry = zipfile.getEntry("feature.xml");
            if (featureXmlEntry == null) {
                throw new IllegalArgumentException("feature.xml missing from zip");
            }
            InputStream in = zipfile.getInputStream(featureXmlEntry);
            if (in == null) {
                throw new IllegalArgumentException("feature.xml cannot be read from zip");
            }
            try {
                String featureXml = IOUtils.toString(in, Charset.forName("UTF-8"));
                JAXB jaxb = new JAXB();
                com.intel.mtwilson.feature.xml.Feature feature = jaxb.read(featureXml, com.intel.mtwilson.feature.xml.Feature.class);
                return feature;
            } catch (IOException | JAXBException | XMLStreamException e) {
                in.close();
            }
        }
        throw new IllegalArgumentException("feature.xml cannot be read");
    }

    private void add(String pathToFeatureZip) throws IOException, JAXBException, XMLStreamException {
        log.debug("Feature zip: {}", pathToFeatureZip);
        File zip = new File(pathToFeatureZip);
        if (zip.exists() && zip.isFile()) {
            // first extract the feature id from the zip file
            com.intel.mtwilson.feature.xml.Feature feature = readFeatureXmlFromZip(zip);
            unzip(zip, new File(Folders.features(feature.getId())));
        }
    }

    private void update(String pathToFeatureZip) {
        log.debug("Feature zip: {}", pathToFeatureZip);
    }

    private void delete(String featureId) throws IOException {
        log.debug("Feature id: {}", featureId);
        File dir = new File(Folders.features(featureId));
        if (!dir.exists()) {
            return; // nothing to do here, feature directory oes not exist
        }
        FileUtils.deleteDirectory(dir);
        log.info("Deleted feature: {}", featureId);
    }

    private void list() {
        log.debug("Feature list");
        ArrayList<com.intel.mtwilson.feature.xml.Feature> features = new ArrayList<>();
        File featureDirectory = new File(Folders.features());
        File[] featureSubdirectories = featureDirectory.listFiles(new DirectoryFilter());
        if (featureSubdirectories != null) {
            for (File featureSubdirectory : featureSubdirectories) {
                File featureXmlFile = featureSubdirectory.toPath().resolve("feature.xml").toFile();
                log.debug("feature.xml: {}", featureXmlFile.getAbsolutePath());
                if (featureXmlFile.exists()) {
                    // read feature.xml and add to list
                    try (FileInputStream in = new FileInputStream(featureXmlFile)) {
                        String featureXml = IOUtils.toString(in, Charset.forName("UTF-8"));
                        JAXB jaxb = new JAXB();
                        com.intel.mtwilson.feature.xml.Feature feature = jaxb.read(featureXml, com.intel.mtwilson.feature.xml.Feature.class);
                        features.add(feature);
                    } catch (IOException | JAXBException | XMLStreamException e) {
                        log.error("Cannot open feature.xml in {}", featureSubdirectory.getAbsolutePath(), e);
                    }

                }
            }
        }
        // display list
        for (com.intel.mtwilson.feature.xml.Feature feature : features) {
            System.out.println(feature.getId());
        }
    }
}
