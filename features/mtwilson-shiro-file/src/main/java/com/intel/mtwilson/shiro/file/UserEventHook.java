/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

/**
 *
 * @author ascrawfo
 */
public interface UserEventHook {
    void afterCreateUser(String username); 
    void afterUpdateUser(String username);
    void afterDeleteUser(String username); 
}
