/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.dcsg.cpg.io.Attributes;

/**
 * An object may implement the Extensions interface in order to allow
 * developers to assign other attributes to the object. This is frequently
 * useful when adding a new or experimental feature to the application which
 * has touch-points at some but not all layers, for EXAMPLE in a web client
 * and in the data storage layer. In this example, the client may send an
 * additional attribute which would be passed along with the object through
 * each layer until being processed by the data store layer. 
 * 
 * NOTE: using extensions means that input validation requires a custom
 * validation class to be defined which also knows about the extension 
 * features. 
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
public interface Extensions {
    Attributes getExtensions();
}
