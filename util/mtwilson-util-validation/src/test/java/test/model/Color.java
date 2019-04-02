/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test.model;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.ObjectModel;
import java.util.List;

/**
 * A model object for testing purposes
 * @author jbuhacoff
 */
public class Color extends ObjectModel {
    private String name;
    private int red;
    private int green;
    private int blue;
    
    /**
     * No-arg constructor to facilitate creation via factory
     */
    public Color() {
        
    }
    
    public Color(String name, int red, int green, int blue) {
        this.name = name;
        this.red = red;
        this.green = green;
        this.blue = blue;
        if(!isValid()) {
            throw new IllegalArgumentException("Name is required");
        }
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    
    public void setRed(int value) {
        this.red = value;
    }
    
    public int getRed() { return red; }
    
    public void setGreen(int value) {
        this.green = value;
    }
    
    public int getGreen() { return green; }

    public void setBlue(int value) {
        this.blue = value;
    }
    
    public int getBlue() { return blue; }

    @Override
    public int hashCode() {
        return String.format("%d%d%d", red, green, blue).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }


    @Override
    protected void validate() {
        if( name == null ) { fault("Name must not be null"); }
    }
    
    
}
