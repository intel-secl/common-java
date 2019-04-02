/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * When Models are used in conjunction with aspect-oriented programming,
 * it's possible to automatically invoke isValid() on models when they
 * are passed to methods. This annotation provides an escape for methods
 * that accept invalid models as input, such as error-handling functions
 * that are prepared to call getFaults() and do something with that information.
 * See the InvalidModelException object for an example use of this annotation.
 *
 * @author jbuhacoff
 */
@Target({ElementType.PARAMETER,ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unchecked {
    
}
