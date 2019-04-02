/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author ssbangal
 */
public class RepositoryCreateConflictException extends RepositoryCreateException {
    
    public RepositoryCreateConflictException() {
        super();
    }

    public RepositoryCreateConflictException(String message) {
        super(message);
    }

    public RepositoryCreateConflictException(Throwable cause) {
        super(cause);
    }

    public RepositoryCreateConflictException(Throwable cause, Locator locator) {
        super(cause, locator);
    }
    
    public RepositoryCreateConflictException(Locator locator) {
        super(locator);
    }
    
    public RepositoryCreateConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
