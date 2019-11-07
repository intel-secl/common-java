/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

/**
 * An object may implement this interface in a multi-tenant system to 
 * identify the user or organization that owns the object.
 * 
 * @since DHSM 
 * @author skamal 
 */
public interface Status{
    String getStatus();
}
