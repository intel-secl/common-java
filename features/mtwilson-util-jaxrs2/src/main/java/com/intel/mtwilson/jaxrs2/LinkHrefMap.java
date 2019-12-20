/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compatibility wrapper which converts map string,link to map string,string
 * @since keplerlake
 * @author jbuhacoff
 */
public class LinkHrefMap implements Map<String,Object> {
    private final Map<String,Link> map;

    public LinkHrefMap(Map<String, Link> links) {
        this.map = links;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        if( !(o instanceof String) ) { return false; }
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        for(Link value : map.values()) {
            if( value == null ) { continue; }
            String href = value.getHref();
            if( href != null && href.equals(o) ) { return true; }
        }
        return false;
    }

    @Override
    public Object get(Object o) {
        if( !(o instanceof String) ) { return null; }
        Link link = map.get((String)o);
        if( link == null ) { return null; }
        return link.getHref();
    }

    @Override
    public Object put(String k, Object v) {
        Object prev = get(k);
        Link link = new Link(v.toString());
        map.put(k,link);
        return prev;
    }

    @Override
    public Object remove(Object o) {
        if( !(o instanceof String) ) { return null; }
        Object prev = get((String)o);
        map.remove(o);
        return prev;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        for(String key : map.keySet()) {
            put(key, map.get(key));
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        ArrayList<Object> values = new ArrayList<>();
        for(Link link : map.values()) {
            values.add(link.getHref());
        }
        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        HashSet<Entry<String,Object>> set = new HashSet<>();
        for(String key : map.keySet()) {
            set.add(new LinkHrefMapEntry(this, key));
        }
        return set;
    }

    public static class LinkHrefMapEntry<K extends String,V> implements Map.Entry {
        private final LinkHrefMap parent;
        private final K key;
        public LinkHrefMapEntry(LinkHrefMap parent, K key) {
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
