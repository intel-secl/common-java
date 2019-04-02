/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author jbuhacoff
 */ 
@Target({ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME) 
@HttpMethod("PATCH") 
public @interface PATCH { 
}
