/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import net.shibboleth.utilities.java.support.xml.ClasspathResolver;

/**
 *
 * @author jbuhacoff
 */
public class ClasspathResourceResolver extends ClasspathResolver /*implements LSResourceResolver*/ {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathResourceResolver.class);
    private String resourcePackage = ""; // by default look in the "default package"; apps can provide a package name under which they have put all the xsd resources
    private String prefix = "";  // the resourcePackage converted to path name, for example package "foo.bar" to prefix "/foo/bar"
    
    public ClasspathResourceResolver() {
        super();
    }

    public String getResourcePackage() {
        return resourcePackage;
    }

    public void setResourcePackage(String resourcePackage) {
        if( resourcePackage == null ) { resourcePackage = ""; }
        this.resourcePackage = resourcePackage;
        this.prefix = pathFromPackageName(this.resourcePackage);
    }
    
    protected String pathFromPackageName(String packageName) {
        return "/"+packageName.replace(".","/");
    }
    // the path is like "/saml-schema-assertion-2.0.xsd"
    protected String pathFromFilename(File file) {
            return prefix+"/"+file.getName();
    }
    // the path is like "/saml-schema-assertion-2.0.xsd"
    protected String pathFromFilename(String filename) {
            return prefix+"/"+filename;
    }
    // the path is like "/security/saml/v2.0/saml-schema-assertion-2.0.xsd"
    protected String pathFromURL(URL url) {
        String path = url.getPath(); // url path is either empty or always includes leading slash
        if( path == null || path.isEmpty() ) { path = "/"; }
        if( path.startsWith("/")) { return prefix+path; }
        return prefix+"/"+path;
    }
    // the path is like "/docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd"
    protected String pathFromURLWithDomain(URL url) {
        return prefix+"/"+url.getHost()+url.getPath(); // url path always includes leading slash
    }

    /**
     * Note that an empty path "" or "/" will be rejected and null will be returned
     * @param path
     * @return a byte array resource or null if the resource was not found
     * @throws IOException 
     */
    private InputStream getResourceAsStream(String path) throws IOException {
        log.debug("findResource: {}", path);
        if( path == null || path.isEmpty() || path.equals("/")) { return null; }
        InputStream in = getClass().getResourceAsStream(path);
        if( in == null ) { return null; }
        return in;
    }

    /**
     * Instead of looking for classpath:/path/to/resource.xsd, this resolver
     * accepts the URLs that are used in the original xsd's and uses the
     * filename at the end of the URL to search in the classpath. 
     * @param publicId
     * @param systemId
     * @return 
     */
    @Override
    protected InputStream resolver(String publicId, String systemId) {
        InputStream in = findResource(systemId);
        if( in == null ) {
            in = findResource(publicId);
        }
        return in;
    }
    
    public InputStream findResource(String href) {
        if( href == null || href.isEmpty() || href.startsWith("-") ) { return null; } // example of startsWith("-") is  -//W3C//DTD XMLSchema 200102//EN
        try {
            
            if( href.startsWith("http") ) { 
                // url like http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd
                URL url = new URL(href);
                String path = pathFromURLWithDomain(url); // the path is like "/docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd"
                log.debug("tried to resolve href {} pathFromURLWithDomain {}", href, path);
                InputStream in = getResourceAsStream(path);
                if( in == null ) {
                    // try again with just the filename instead of full path from url
                    path = pathFromFilename(new File(url.getPath())); // the path is like "/saml-schema-assertion-2.0.xsd"
                    log.debug("tried to resolve href {} pathFromFilename {}",href, path);
                    in = getResourceAsStream(path);
                }
                log.debug("tried to resolve href {} to {}", href, in);
                return in;
            }
            else {
                // plain filename like saml-schema-assertion-2.0.xsd
                String path = pathFromFilename(href);
                InputStream in = getResourceAsStream(path);// path like "/saml-schema-assertion-2.0.xsd"
                log.debug("tried to resolve href {} to {}", href, in);
                return in;
            }
        }
        catch(IOException e) {
            log.error("Cannot open input stream: {}: {}", href, e.getMessage());
            return null;
        }
    }
}
