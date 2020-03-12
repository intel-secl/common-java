/*
 * Copyright (C) 2020 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * XmlAnnotationIntrospector is a custom XML annotation introspector for 
 * ensuring that select response fields can be suppressed in XML output
 */
public class XmlAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return m.hasAnnotation(JsonOnly.class) || super.hasIgnoreMarker(m);
    }
}
