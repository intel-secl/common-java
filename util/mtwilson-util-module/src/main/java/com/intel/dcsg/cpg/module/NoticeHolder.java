/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.module;

/**
 *
 * @author jbuhacoff
 */
public class NoticeHolder {
    private Object wrappedObject;
    private ComponentHolder component;

    public NoticeHolder(Object wrappedObject, ComponentHolder component) {
        this.wrappedObject = wrappedObject;
        this.component = component;
    }

    public ComponentHolder getComponent() {
        return component;
    }

    public Object getWrappedObject() {
        return wrappedObject;
    }
    
    
}
