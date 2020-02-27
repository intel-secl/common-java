/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.mtwilson.jaxrs2.Link;
import java.util.Collection;
import java.util.Map;

/**
 * An object can include hints to the client about possible 
 * state transitions in the form of links. Each transition is
 * identified by its "relation" to the object, and specified by
 * the link.
 * 
 * The "self" link is NOT included in this map. Objects with a "self"
 * link should implement the getHref() function directly for a URL,
 * and the getURI() function for a URI.
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
public interface Links {
    
    /**
     * 
     * @return the complete map of available links
     */
    Map<String,Link> getLinkMap();
    
    /**
     * 
     * @param relation
     * @return the specified link, or null if the relation is not defined
     */
    Link getLink(String relation);
}
