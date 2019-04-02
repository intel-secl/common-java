/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class SquareViewer extends ShapeViewer {
    private Square square;
    public SquareViewer(Square square) {
        this.square = square;
    }
    @Override
    public String getShapeDescription() {
        return String.format("%s square with length %d", square.getColor(), square.getLength());
    }
    
}
