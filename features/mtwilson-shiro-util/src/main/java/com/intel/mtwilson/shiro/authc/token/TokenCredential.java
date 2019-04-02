/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.token;

import java.util.Date;

/**
 * Represents the registered token value with expiration date and usage
 * information. This can be compared to the token in the request.
 * 
 * The notBefore and notAfter dates define the validity period of the token.
 * It is invalid before the notBefore date and after the notAfter date.
 * 
 * The used number indicates how many times the token has been used. When the
 * token is first created, the used count is zero. After it is used the first
 * time, the used count is 1. 
 * 
 * The usedMax number indicates how many times the token can be used before
 * it becomes invalid due to too many uses. A one-time use token will have
 * a usedMax value of 1.
 * 
 * The {@code use()} method should be called by authentication code that has
 * validated a token in a request against this credential; the lookup service
 * should return a subclass that may implement a callback to increment the
 * used count in the database.
 * 
 * The TokenCredential is intentionally read-only. Applications can request
 * the framework to extend/renew or replace a token (and receive a new instance)
 * but not to attempt to modify an existing token outside the framework.
 * 
 * @author jbuhacoff
 */
public class TokenCredential {
    private String value;
    private Date notBefore;
    private Date notAfter;
    private Integer used;
    private Integer usedMax;
    private Long keepalive; // milliseconds

    public TokenCredential() {
        this.value = null;
        this.notBefore = null;
        this.notAfter = null;
        this.used = null;
        this.usedMax = null;
        this.keepalive = null;
    }

    public TokenCredential(String value, Date notBefore, Date notAfter, Integer used, Integer usedMax) {
        this.value = value;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.used = used;
        this.usedMax = usedMax;
    }

    public TokenCredential(String value, Date notBefore, Date notAfter, Integer used, Integer usedMax, Long keepalive) {
        this.value = value;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.used = used;
        this.usedMax = usedMax;
        this.keepalive = keepalive;
    }
    
    public String getValue() {
        return value;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public Integer getUsed() {
        return used;
    }

    public Integer getUsedMax() {
        return usedMax;
    }

    public Long getKeepalive() {
        return keepalive;
    }
    
    
    public void use() {
        if( used == null ) { used = 0; }
        used++;
    }

}
