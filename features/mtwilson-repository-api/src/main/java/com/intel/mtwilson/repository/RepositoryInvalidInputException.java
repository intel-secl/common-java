/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryInvalidInputException extends RepositoryException {
    private Locator locator;
    
    public RepositoryInvalidInputException() {
        super();
    }

    public RepositoryInvalidInputException(String message) {
        super(message);
    }

    public RepositoryInvalidInputException(Throwable cause) {
        super(cause);
    }

    public RepositoryInvalidInputException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }
    
    public RepositoryInvalidInputException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public RepositoryInvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public Locator getLocator() {
        return locator;
    }

}
