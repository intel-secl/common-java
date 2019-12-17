/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

/**
 *
 * @author ascrawfo
 */
public interface UserEventHook2 {
    void beforeCreateUser(String username, char[] password); 
    void afterCreateUser(String username, char[] password); 
    void beforeChangePassword(String username, char[] oldPassword, char[] newPassword);
    void afterChangePassword(String username, char[] oldPassword, char[] newPassword);
    void beforeDeleteUser(String username);
}
