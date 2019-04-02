/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.servlet;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class ClasspathFileServlet extends HttpServlet {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathFileServlet.class);

    /**
     * No trailing slash because getPathInfo() always has a leading slash so
     * requests look like "/index.html" so after concatenating the path would be
     * "/www/index.html"
     */
    private String relativePath = "/www";  // should be changed to "/publicResources"

    // wait for a GET from client, then perform these actions
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("RequestURI: " + req.getRequestURI());  //  RequestURI: /file/index.html
        System.out.println("ContextPath: " + req.getContextPath()); // ContextPath:  (empty when accessing /file/index.html)
        System.out.println("PathInfo: " + req.getPathInfo()); // PathInfo: /index.html     (because this servlet is rooted at /file so this is relative to servlet)
        System.out.println("PathTranslated: " + req.getPathTranslated());  // PathTranslated: null
        System.out.println("RemoteAddr: " + req.getRemoteAddr()); // RemoteAddr: 127.0.0.1
        System.out.println("Scheme: " + req.getScheme()); // Scheme: https
        System.out.println("ServletPath: " + req.getServletPath()); // ServletPath: /file
        System.out.println("ServerName: " + req.getServerName()); // ServerName: 127.0.0.1

        // no path separator because getPathInfo() always has leading slash
        InputStream in = getClass().getResourceAsStream(relativePath + req.getPathInfo());
        if (in == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            try {
                resp.setStatus(HttpServletResponse.SC_OK);
                OutputStream out = resp.getOutputStream();
                IOUtils.copy(in, out);
            } finally {
                try { in.close(); } catch (IOException e) { log.warn("Failed to close InputStream", e); }
            }
        }
    }
}
