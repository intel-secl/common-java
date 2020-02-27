/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

/**
 * An object implementing the Id interface has a unique identifier. It is NOT
 * the responsibility of the object to ensure the identifier is unique. The
 * application must verify this at the time the identifier is assigned to
 * the object.
 * 
 * If the object also implements the Realm interface, then the Id is only
 * expected to be unique within the Realm. 
 * 
 * An id may frequently be a UUID but is not required to be a UUID.
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
public interface Id {
    String getId();
}
