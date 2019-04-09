/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroFilter;

/**
 * When using ShiroFilter, the subject is supposed to be automatically cleared
 * after the request processing {@link https://shiro.apache.org/subject.html}.
 * If it appears this isn't happening you can use this LoggingShiroFilter to
 * inspect the subject immediately before and after request execution.
 *
 * @author jbuhacoff
 */
public class LoggingShiroFilter extends ShiroFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingShiroFilter.class);

    @Override
    protected void executeChain(ServletRequest request, ServletResponse response, FilterChain origChain)
            throws IOException, ServletException {
        Subject subject = SecurityUtils.getSubject();
        log.debug("before executeChain subject authenticated? {}", subject.isAuthenticated()); // should be false!    but is true on second request...
        super.executeChain(request, response, origChain);
        log.debug("after executeChain subject authenticated? {}", subject.isAuthenticated()); // should be true!
        subject.logout();
        log.debug("after executeChain logout subject authenticated? {}", subject.isAuthenticated()); // should be true!
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        try {
            log.debug("before doFilterInternal");
            // parent class initializes the subject in its doFilterInternal so there is nothing to log before parent executes
            super.doFilterInternal(servletRequest, servletResponse, chain);
            log.debug("after doFilterInternal; subject not bound to thread at this point");
        } finally {
            log.debug("finally after doFilterInternal; subject not bound to thread at this point");
        }
    }
}
