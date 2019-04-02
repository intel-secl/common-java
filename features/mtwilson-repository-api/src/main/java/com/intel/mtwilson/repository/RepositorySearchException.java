/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositorySearchException extends RepositoryException {
    private FilterCriteria query = null;
    
    public RepositorySearchException() {
        super();
    }

    public RepositorySearchException(String message) {
        super(message);
    }

    public RepositorySearchException(Throwable cause) {
        super(cause);
    }

    public RepositorySearchException(Throwable cause, FilterCriteria query) {
        super(cause);
        this.query = query;
    }
    
    public RepositorySearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterCriteria getQuery() {
        return query;
    }
    
    

}
