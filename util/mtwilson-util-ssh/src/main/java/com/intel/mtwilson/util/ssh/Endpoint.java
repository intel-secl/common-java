/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.ssh;

import java.util.Objects;

/**
 *
 * @author jbuhacoff
 */
public class Endpoint {
    private String host;
    private int port;
    private String username;

    public Endpoint(String host) {
        this(host,22,"root");
    }
    
    public Endpoint(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        Endpoint other = (Endpoint) obj;
        String otherHost = other.getHost();
        int otherPort = other.getPort();
        String otherUsername = other.getUsername();
        boolean equalHost = false;
        boolean equalPort = false;
        boolean equalUsername = false;
        if (host == null && otherHost == null || host != null && otherHost != null && host.equals(otherHost)) {
            equalHost = true;
        }
        if (port == otherPort) {
            equalPort = true;
        }
        if (username == null && otherUsername == null || username != null && otherUsername != null && username.equals(otherUsername)) {
            equalUsername = true;
        }
        return equalHost && equalPort && equalUsername;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.host);
        hash = 31 * hash + Objects.hashCode(this.port);
        hash = 31 * hash + Objects.hashCode(this.username);
        return hash;
    }
}
