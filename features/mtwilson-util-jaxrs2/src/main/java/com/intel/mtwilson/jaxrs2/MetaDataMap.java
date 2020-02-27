/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.validation.Fault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class exists as a compatibility layer between the original
 * @{code Map<String,Object> getMeta()} function and the new 
 * @{code MetaObject getMeta()} function.
 * 
 * @since keplerlake
 * @author jbuhacoff
 */
public class MetaDataMap extends MetaData implements Map<String,Object> {
    private final MetaData meta;
    public MetaDataMap(MetaData meta) {
        this.meta = meta;
    }

    @Override
    public int size() {
        //meta.getRealm()
        //meta.getId()
        //meta.getType()
        return 3 + meta.getExtensions().map().size();
    }
    
    private boolean empty(String value) {
        return value == null || value.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return empty(meta.getRealm()) && empty(meta.getId()) && empty(meta.getType()) && meta.getExtensions().map().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if( !(key instanceof String) ) { return false; }
        if( key.equals("realm") && !empty(meta.getRealm()) ) { return true; }
        if( key.equals("id") && !empty(meta.getId()) ) { return true; }
        if( key.equals("type") && !empty(meta.getType()) ) { return true; }
        return meta.getExtensions().map().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if( value == null ) { return false; }
        if( value.equals(meta.getRealm()) ) { return true; }
        if( value.equals(meta.getId()) ) { return true; }
        if( value.equals(meta.getType()) ) { return true; }
        return meta.getExtensions().map().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if( !(key instanceof String) ) { return false; }
        if( key.equals("realm") ) { return meta.getRealm(); }
        if( key.equals("id") ) { return meta.getId(); }
        if( key.equals("type") ) { return meta.getType(); }
        return meta.getExtensions().map().get(key);
    }

    @Override
    public Object put(String key, Object value) {
        if( key.equals("realm") ) { 
            if( !(value instanceof String) ) { throw new IllegalArgumentException("value must be a string"); }
            String prev = meta.getRealm(); meta.setRealm((String)value); return prev; 
        }
        if( key.equals("id") ) {
            if( !(value instanceof String) ) { throw new IllegalArgumentException("value must be a string"); }
            String prev = meta.getId(); meta.setId((String)value); return prev; 
        }
        if( key.equals("type") ) {
            if( !(value instanceof String) ) { throw new IllegalArgumentException("value must be a string"); }
            String prev = meta.getType(); meta.setType((String)value); return prev; 
        }
        return meta.getExtensions().map().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        if( !(key instanceof String) ) { return null; }
        if( key.equals("realm") ) { 
            String prev = meta.getRealm(); meta.setRealm(null); return prev; 
        }
        if( key.equals("id") ) {
            String prev = meta.getId(); meta.setId(null); return prev; 
        }
        if( key.equals("type") ) {
            String prev = meta.getType(); meta.setType(null); return prev; 
        }
        return meta.getExtensions().map().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        for(String key : map.keySet()) {
            put(key, map.get(key));
        }
    }

    @Override
    public void clear() {
        meta.setRealm(null);
        meta.setId(null);
        meta.setType(null);
        meta.getExtensions().map().clear();
    }

    /**
     * Changes to the collection ARE NOT reflected in the map.
     * 
     * @return 
     */
    @Override
    public Set<String> keySet() {
        HashSet<String> set = new HashSet<>();
        set.add("realm");
        set.add("id");
        set.add("type");
        set.addAll(meta.getExtensions().map().keySet());
        return set;
    }

    /**
     * Changes to the collection ARE NOT reflected in the map.
     * 
     * @return 
     */
    @Override
    public Collection<Object> values() {
        ArrayList<Object> values = new ArrayList<>();
        values.add(meta.getRealm());
        values.add(meta.getId());
        values.add(meta.getType());
        values.addAll(meta.getExtensions().map().values());
        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        HashSet<Entry<String,Object>> set = new HashSet<>();
        set.add(new MetaObjectMapEntry<>(this,"realm"));
        set.add(new MetaObjectMapEntry<>(this,"id"));
        set.add(new MetaObjectMapEntry<>(this,"type"));
        set.addAll(meta.getExtensions().map().entrySet());
        return set;
    }

    public static class MetaObjectMapEntry<K extends String,V> implements Map.Entry {
        private final MetaDataMap parent;
        private final K key;
        public MetaObjectMapEntry(MetaDataMap parent, K key) {
            this.parent = parent;
            this.key = key;
        }
        
        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return parent.get(key);
        }

        @Override
        public Object setValue(Object arg0) {
            return parent.put(key, arg0);
        }
    }
    
}
