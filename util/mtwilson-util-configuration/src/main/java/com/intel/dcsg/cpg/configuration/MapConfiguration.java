/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class MapConfiguration extends AbstractConfiguration {

    private boolean editable = true;
    private Map<String, String> map;

    public MapConfiguration() {
        this(new HashMap<String, String>());
    }

    /**
     * The map should be editable; if a read-only map is provided use the other
     * constructor to set editable property to false.
     *
     * @param map
     */
    public MapConfiguration(Map<String, String> map) {
        super();
        this.map = map;
    }

    public MapConfiguration(Map<String, String> map, boolean editable) {
        super();
        this.map = map;
        this.editable = editable;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    @Override
    public void set(String key, String value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Creates a new HashMap from any Configuration instance
     * and copies all the settings to it
     * 
     * @param configuration
     * @return 
     */
    public static HashMap<String, String> toHashMap(Configuration configuration) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : configuration.keys()) {
            map.put(key, configuration.get(key));
        }
        return map;
    }
}
