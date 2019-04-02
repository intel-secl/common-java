/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryCreateException extends RepositoryException {
    private Locator locator;
    
    public RepositoryCreateException() {
        super();
    }

    public RepositoryCreateException(String message) {
        super(message);
    }

    public RepositoryCreateException(Throwable cause) {
        super(cause);
    }

    public RepositoryCreateException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryCreateException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public Locator getLocator() {
        return locator;
    }
    
}
