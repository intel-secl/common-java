/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class JsonPathTest {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonPathTest.class);

    public static String readString(String filename) throws IOException {
        if (filename == null) {
            return null;
        }
        try (InputStream in = JsonPathTest.class.getResourceAsStream(String.format("/%s", filename))) {
            return IOUtils.toString(in, UTF8);
        }
    }
    
    @Test
    public void testReadMetaId() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        String id = jsonpath.getString(input, "$.meta.id");
        log.debug("document: {}", input);
        log.debug("expression: $.meta.id");
        log.debug("value: {}", id);
    }
    
    @Test
    public void testWriteMetaAuthor() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        String edited = jsonpath.putString(input, "$.meta", "author", "jonathan");
        log.debug("document: {}", input);
        log.debug("parent: $.meta");
        log.debug("key: {}, value: {}", "author", "jonathan");
        log.debug("after editing: {}", edited);
        assertEquals("jonathan", jsonpath.getString(edited, "$.meta.author"));
    }

    @Test
    public void testReadDerivationTypeHref() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        String expression = String.format("$.derivation.%s.href", "openssl");
        String id = jsonpath.getString(input, expression);
        log.debug("document: {}", input);
        log.debug("expression: {}", expression);
        log.debug("value: {}", id);
    }
    
    
    @Test
    public void testReadFirstItem() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        String label = jsonpath.getString(input, "$.items[0].label");
        log.debug("document: {}", input);
        log.debug("expression: $.items[0].label");
        log.debug("value: {}", label);
    }

    @Test
    public void testReplaceListWithArray() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        ArrayList<String> list = new ArrayList<>();
        list.add("first item");
        list.add("second item");
        String edited = jsonpath.putStringList(input, "$", "items", list); // replaces the list "items": [ {"label":"first item"}, {"label":"second item"} ]  with a list of strings "items": [ "first item", "second item" ]
        log.debug("document: {}", input);
        log.debug("edited: {}", edited);
        assertEquals("second item", jsonpath.getString(edited, "$.items[1]"));
    }
    
    @Test
    public void testReplaceObject() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        Meta meta = new Meta();
        meta.id = "5678";
        meta.author = "jonathan";
        ObjectMapper mapper = new ObjectMapper();
        String jsonValue = mapper.writeValueAsString(meta);
        String edited = jsonpath.putJsonObject(input, "$", "meta", jsonValue);
        log.debug("document: {}", input);
        log.debug("edited: {}", edited);
        assertEquals("5678", jsonpath.getString(edited, "$.meta.id"));
        assertEquals("jonathan", jsonpath.getString(edited, "$.meta.author"));
    }

    public static class Meta { public String id; public String author; }
    
    @Test
    public void testReadAllItems() throws ScriptException, NoSuchMethodException, IOException {
        String input = readString("com/intel/mtwilson/json/document1.json");
        JsonPath jsonpath = new JsonPath();
        /*ItemsHolder itemsHolder*/// ItemsHolder items = new ItemsHolder();
        Item[] items = jsonpath.getObject(Item[].class, input, "$.items");
        log.debug("document: {}", input);
        log.debug("expression: $.items[0].label");
        log.debug("first label: {}",items[0].label);
    }
    
    public static class ItemsHolder {
        public List<Item> items;
    }
    public static class Item {
        public String label;
    }
}
