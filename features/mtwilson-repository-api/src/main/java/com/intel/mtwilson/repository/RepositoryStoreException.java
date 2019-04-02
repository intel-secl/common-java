/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryStoreException extends RepositoryException {
    private Locator locator;

    public RepositoryStoreException() {
        super();
    }

    public RepositoryStoreException(String message) {
        super(message);
    }

    public RepositoryStoreException(Throwable cause) {
        super(cause);
    }

    public RepositoryStoreException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public Locator getLocator() {
        return locator;
    }
   
}
