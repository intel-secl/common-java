/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console.input;

import com.intel.dcsg.cpg.validation.InputModel;
import org.apache.commons.lang3.Range;
/**
 *
 * @author jbuhacoff
 */
public class IntegerInput extends InputModel<Integer> {

    private final Range<Integer> range;
    public IntegerInput() { 
        range = null; 
    }
    public IntegerInput(Range<Integer> intRange) {
        this.range = intRange;
    }
    public IntegerInput(int min, int max) {
        this.range = Range.between(min, max);
    }
    
    @Override
    protected Integer convert(String input) {
        try {
            Integer value = Integer.valueOf(input);
            if( range == null ) {
                return value;
            }
            else {
                if( range.contains(value) ) {
                    return value;
                }
                else {
                    fault("Not in range [%d, %d]: %d", range.getMinimum(), range.getMaximum(), value);
                }
            }
        }
        catch(java.lang.NumberFormatException e) {
            fault(e, "Not a number: %s", input);
        }
        return null;
    }
    

}
