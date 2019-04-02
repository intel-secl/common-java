/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class CircleViewer extends ShapeViewer {
    private Circle circle;
    public CircleViewer(Circle circle) {
        this.circle = circle;
    }
    @Override
    public String getShapeDescription() {
        return String.format("%s circle with radius %d", circle.getColor(), circle.getRadius());
    }
    
}
