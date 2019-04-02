/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.host;

/**
 *
 * @author jbuhacoff
 */
public interface HostFilter {
    boolean accept(String address);    
}
