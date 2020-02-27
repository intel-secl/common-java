/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.keplerlake.authz.hmac;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * Represents a list of key-value pairs for a query string or http header.
 *
 * @author jbuhacoff
 */
public class KeyValueList {

    private final ArrayList<KeyValue> list = new ArrayList<>();

    public void add(String key, String value) {
        list.add(new KeyValue(key, value));
    }

    public void add(KeyValue item) {
        list.add(item);
    }

    public ArrayList<KeyValue> list() {
        return list;
    }
    
    /**
     * @return a query string with all items in the list, sorted by key and
     * value.
     */
    public String toQueryString() {
        ArrayList<String> items = new ArrayList<>();
        for (KeyValue kv : list) {
            items.add(kv.toQueryString());
        }
        Collections.sort(items);
        return StringUtils.join(items, "&");
    }

    /**
     *
     * @return an http header parameter string with all items in the list, sorted by key and
     * value.
     */
    public String toParameterString() {
        ArrayList<String> items = new ArrayList<>();
        for (KeyValue kv : list) {
            items.add(kv.toParameterString());
        }
        Collections.sort(items);
        return StringUtils.join(items, ", ");
    }

}
