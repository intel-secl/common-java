/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.validation;

import java.util.List;

/**
 * This convenience class facilitates serializing any Model instance and its validity status.
 * The ObjectModel implementation of Model intentionally hides the isValid() and getFaults()
 * fields from serialization.  This class is not a Model instance itself but only reports
 * on the state of another Model - therefore it can be passed around without triggering any
 * automated model-checking exceptions when the Model it is reporting is invalid.
 * 
 * @author jbuhacoff
 */
public class Report {
    private Model model;
    
    public Report(Model model) {
        this.model = model;
    }
    
    public boolean isValid() {
        return model.isValid();
    }
    
    public List<Fault> getFaults() {
        return model.getFaults();
    }
}
