/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions.contextstyle;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ContextSensitivePluginFinderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSensitivePluginFinderTest.class);

    @Test
    public void testEasyScanWithPluginAnnotation() {
        ExtensionUtil.scan(new ImplementationRegistrar(), CircleViewer.class, SquareViewer.class, TriangleViewer.class); 
    }
    
    private void viewShape(Shape shape) {
        log.debug("getting viewer for shape {}", shape.getName());
        ShapeViewer viewer = Extensions.find(ShapeViewer.class, shape);
        if( viewer == null ) {
            log.debug("did not find viewer");
            return;
        }
        log.debug("found viewer {}", viewer.getClass().getName());
        log.debug("VIEW: {}", viewer.getShapeDescription());
    }
    
    @Test
    public void testUsePlugins() {
        // initialize whiteboard
        testEasyScanWithPluginAnnotation();
        // now pretend to do something useful that requires shape plugins
        viewShape(new Circle());
        viewShape(new Square());
        viewShape(new Triangle());
        WhiteboardExtensionProvider.clear(TriangleViewer.class);
        viewShape(new Triangle());  // should not find it
        WhiteboardExtensionProvider.register(ShapeViewer.class, TriangleViewer.class);
        viewShape(new Triangle()); // should find it again
        WhiteboardExtensionProvider.clearAll();
        viewShape(new Triangle()); // should not find it
    }
    
}
