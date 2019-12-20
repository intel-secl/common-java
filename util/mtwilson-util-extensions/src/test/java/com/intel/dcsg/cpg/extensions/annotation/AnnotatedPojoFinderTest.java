/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.annotation;
import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class AnnotatedPojoFinderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnnotatedPojoFinderTest.class);

    @Test
    public void testEasyScanWithInterface() {
        log.debug("Scanning");
        ExtensionUtil.scan(new AnnotationRegistrar(), Apple.class, Carrot.class, GreenApple.class, Fruit.class); // should ignore Fruit interface and register only the implementations
    }
    
    private void eatFruit(String inputFruitName) {
        log.debug("Finding all {} fruits", inputFruitName);
        List<Object> fruits = Extensions.findAllAnnotated(Fruit.class);
        for(Object fruit : fruits) {
            log.debug("Looking at fruit name: {}", 
                    fruit.toString());
            if( inputFruitName.equals(fruit.toString())) {
                log.debug("Found {}", fruit.toString());
            }
        }        
    }
    
    @Test
    public void testUsePlugins() {
        WhiteboardExtensionProvider.clearAll();
        // initialize whiteboard
        testEasyScanWithInterface();
        // now pretend to do something useful that requires fruit plugins
        eatFruit("red apple");
        eatFruit("green apple");// will not be found
        eatFruit("orange carrot");// will not be found
        // runtime addition of new plugin
        WhiteboardExtensionProvider.registerAnnotated(Fruit.class, Banana.class);
        // now try to use it
        eatFruit("yellow banana");
        // runtime clearing of available plugins
        WhiteboardExtensionProvider.clear(Fruit.class);
        eatFruit("yellow banana"); // output: No registered implementations for com.intel.dcsg.cpg.whiteboard.Fruit
    }
}
