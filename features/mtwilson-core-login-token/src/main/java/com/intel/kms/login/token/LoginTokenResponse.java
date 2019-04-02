/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.kms.login.token;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
public class LoginTokenResponse implements Faults {
    private String authorizationToken;
    private Iso8601Date authorizationDate;
    private Iso8601Date notAfter;
    private ArrayList<Fault> faults = new ArrayList<>();

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public Iso8601Date getAuthorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(Iso8601Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    public Iso8601Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Iso8601Date notAfter) {
        this.notAfter = notAfter;
    }

    @Override
    public Collection<Fault> getFaults() {
        return faults;
    }
    
    protected void fault(Fault fault) {
        faults.add(fault);
    }
    
}
