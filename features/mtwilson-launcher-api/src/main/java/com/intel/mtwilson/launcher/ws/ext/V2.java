/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.launcher.ws.ext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with @V2 and @Path("path") can be accessed at a URL 
 * like /v2/{path} and indicates they follow the Mt Wilson 2.x API conventions
 * 
 * @author jbuhacoff
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface V2 {
    
}
