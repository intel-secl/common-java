/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

/**
 * An object may implement this interface in a multi-tenant system to 
 * identify the user or organization that owns the object.
 * 
 * @since DHSM 
 * @author skamal 
 */
public interface Operation{
    String getOperation();
}
