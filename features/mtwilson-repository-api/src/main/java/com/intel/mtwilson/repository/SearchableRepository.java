/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 * @author jbuhacoff
 */
public interface SearchableRepository<T, L extends Locator<T>, C, F extends FilterCriteria<T>> extends Repository<T,L> {
    /**
     * Given criteria encapsulated in a POJO, returns a collection of items
     * matching the criteria.
     *
     * @param criteria
     * @return
     */
    C search(F criteria);
    
    /**
     * Given criteria encapsulated in a POJO, deletes all items in repository
     * matching the criteria.
     * @param criteria 
     */
    void delete(F criteria);
}
