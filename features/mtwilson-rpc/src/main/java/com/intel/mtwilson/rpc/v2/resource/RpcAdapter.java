/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.dcsg.cpg.validation.Fault;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface RpcAdapter<T,U> {
    Class<? extends T> getInputClass();
    Class<? extends U> getOutputClass();
    void setInput(T input);
    void invoke();
    U getOutput();
    List<Fault> getFaults();
}
