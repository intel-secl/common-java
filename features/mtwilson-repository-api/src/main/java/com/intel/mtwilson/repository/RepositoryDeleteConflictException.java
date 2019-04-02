/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryDeleteConflictException extends RepositoryDeleteException {
    private Locator locator;
    
    public RepositoryDeleteConflictException() {
        super();
    }

    public RepositoryDeleteConflictException(String message) {
        super(message);
    }

    public RepositoryDeleteConflictException(Throwable cause) {
        super(cause);
    }

    public RepositoryDeleteConflictException(Throwable cause, Locator locator) {
        super(cause);
        this.locator = locator;
    }

    public RepositoryDeleteConflictException(Locator locator) {
        super();
        this.locator = locator;
    }
    
    public RepositoryDeleteConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Locator getLocator() {
        return super.getLocator();
    }

}
