/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jbuhacoff
 */
public class RateLimitFilter implements Filter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitFilter.class);
    private static final HashMap<String, Client> CLIENT_MAP = new HashMap<>();
    private static final Integer MAX_CLIENT_MAP_SIZE = 8192;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        log.debug("init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String remote = request.getRemoteAddr();
        log.debug("Remote address {}", remote);
        Date now = new Date();
        // clean up the cache when it becomes big
        if( CLIENT_MAP.size() > MAX_CLIENT_MAP_SIZE ) {
            ArrayList<String> toRemove = new ArrayList<>();
            for(String item : CLIENT_MAP.keySet()) {
                Client client = CLIENT_MAP.get(remote);
                if( client == null ||
                     (client.locked && client.lockExpires.before(now))
                     ) {
                    toRemove.add(item);
                }
            }
            for(String item : toRemove) {
                CLIENT_MAP.remove(item);
            }
        }
        Client client = CLIENT_MAP.get(remote);
        // check if client has an expired lock        
        if (client != null && client.locked && client.lockExpires.before(now)) {
            client.locked = false;
            client.lockExpires = null;
            client.attempts = 0;
            CLIENT_MAP.remove(remote);
            client = null;
        }
        // check if client is already locked
        if (client != null && client.locked) {
            // any attempts while locked just extend the lock period by 5 seconds
            if (client.lockExpires == null) {
                client.lockExpires = now;
            }
            client.attempts += 1;
            client.lockExpires.setTime(client.lockExpires.getTime() + 5000);
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.reset();
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.addHeader("Rate-Limit-Expires", client.lockExpires.toString());
            } else {
                response.reset();
                log.error("ServletResponse is not HttpServletResponse (request)");
            }
            return;
        }
        // allow request to continue
        chain.doFilter(request, response);
        // look for 401 or 403 response
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            int status = httpResponse.getStatus();
            if (status == 401 || status == 403) {
                if (client == null) {
                    client = new Client();
                    client.attempts += 1;
                    CLIENT_MAP.put(remote, client);
                } else {
                    client.attempts += 1;
                    if (client.attempts > 5) {
                        client.locked = true;
                        if (client.lockExpires == null) {
                            client.lockExpires = new Date();
                        }
                        client.lockExpires.setTime(client.lockExpires.getTime() + (client.attempts * 5000));
                    }
                }
            }
        } else {
            response.reset();
            log.error("ServletResponse is not HttpServletResponse (response)");
        }

    }

    @Override
    public void destroy() {
        log.debug("destroy");
    }

    public static class Client {

        public Integer attempts = 0;
        public boolean locked = false;
        public Date lockExpires = null;
    }

}
