/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

/**
 * An object can implement Type to indicate a URI for its type so that it can
 * be serialized/de-serialized across multiple languages and API versions while
 * retaining the same semantics defined by the URI.
 * 
 * Java serialization utilities such as Jackson can write the Java class name
 * as a type, and this can be made to translate to Javascript without too much
 * hassle, but is less natural for Python and other languages, and also doesn't
 * survive a Java refactoring. However, by using a URI namespace for the types,
 * we can maintain an equivalent mapping in any language and update it when
 * refactoring. It also allows the data to be serialized and processed without
 * necessarily having a corresponding class instance, and without losing the
 * type information.  
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
public interface Type {
    String getType();
}
