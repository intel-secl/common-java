/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClassNameTest {
    @Test
    public void testClassName() {
        System.out.println("getName: "+ClassNameTest.class.getName());
        System.out.println("getSimpleName: "+ClassNameTest.class.getSimpleName());
        System.out.println("getCanonicalName: "+ClassNameTest.class.getCanonicalName());
    }
}
