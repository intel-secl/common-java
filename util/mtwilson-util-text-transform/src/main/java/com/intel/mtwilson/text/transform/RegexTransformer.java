/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.text.transform;

import com.intel.mtwilson.pipe.Transformer;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class RegexTransformer implements Transformer<String> {
    private Map<String,String> replacements;

    public RegexTransformer(Map<String,String> replacements) {
        this.replacements = replacements;
    }
    
    
    @Override
    public String transform(String input) {
        String result = input;
        for(Map.Entry<String,String> entry : replacements.entrySet()) {
            result = result.replaceAll(entry.getKey(), entry.getValue()); // key is regex search, value is replacement
        }
        return result;
    }
    
}
