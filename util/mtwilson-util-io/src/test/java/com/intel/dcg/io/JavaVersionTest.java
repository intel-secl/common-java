/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.JavaVersion;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class JavaVersionTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaVersionTest.class);

    @Test
    public void testVersion() {
        JavaVersion version = new JavaVersion("1.8.0_92");

        assertFalse( version.isAtLeast(1, 9) );
        
        assertTrue( version.isAtLeast(1, 8) );
        assertTrue( version.isAtLeast(1, 7) );
        assertTrue( version.isAtLeast(1, 6) );
        
        assertFalse( version.isAtLeast(1,8,1));
        
        assertTrue( version.isAtLeast(1,8,0));
        
        assertFalse( version.isAtLeast(1,8,0,999));
        
        assertTrue( version.isAtLeast(1,8,0,92));
        assertTrue( version.isAtLeast(1,8,0,89));
        assertTrue( version.isAtLeast(1,8,0,56));
        assertTrue( version.isAtLeast(1,8,0,0));
        
        log.debug("Current runtime is: {}.{}", JavaVersion.runtime().getMajor(), JavaVersion.runtime().getMinor()); // "1.8"
    }
}
