/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.io.IOUtils;

/**
 * This is a tiny implementation of a tiny subset of jsonpath. It only works
 * for a few simple usages like $.meta.id or $.items[0].label which can be
 * interpreted as javascript. 
 * @author jbuhacoff
 */
public class JsonPath {
    private final static Charset utf8 = Charset.forName("UTF-8");
    private final ScriptEngine javascriptEngine;

    public JsonPath() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        javascriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        try (InputStream in = JsonPath.class.getResourceAsStream("/com/intel/mtwilson/json/jsonpath.js")) {
            String workflowJsonUtilJs = IOUtils.toString(in, utf8);
            javascriptEngine.eval(workflowJsonUtilJs); // throws ScriptException
        } catch (ScriptException | IOException e) {
            throw new IllegalStateException("Initialization failed", e);
        }
    }
    
    public <T> T getObject(Class<T> clazz, String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        Invocable invocable = (Invocable) javascriptEngine;
        String resultJson = (String) invocable.invokeFunction("getValueAtPath", json, expression);
        if (resultJson == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultJson, clazz);
    }
    
    public String putObject(String json, String parentExpression, String key, Object value) throws ScriptException, NoSuchMethodException, IOException {
        Invocable invocable = (Invocable)javascriptEngine;
        String resultJson = (String)invocable.invokeFunction("putValueAtPath", json, parentExpression, key, value);
        return resultJson;
    }
    
    public String putJsonObject(String json, String parentExpression, String key, String jsonValue) throws ScriptException, NoSuchMethodException, IOException {
        Invocable invocable = (Invocable)javascriptEngine;
        String resultJson = (String)invocable.invokeFunction("putJsonValueAtPath", json, parentExpression, key, jsonValue);
        return resultJson;
    }
    
    public String getString(String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        return getObject(String.class, json, expression);
    }
    
    public String putString(String json, String parentExpression, String key, String value) throws ScriptException, NoSuchMethodException, IOException {
        return putObject(json, parentExpression, key, value);
    }
    
    public Integer getInteger(String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        return getObject(Integer.class, json, expression);
    }

    public String putInteger(String json, String parentExpression, String key, Integer value) throws ScriptException, NoSuchMethodException, IOException {
        return putObject(json, parentExpression, key, value);
    }

    public Boolean getBoolean(String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        return getObject(Boolean.class, json, expression);
    }
    
    public String putBoolean(String json, String parentExpression, String key, Boolean value) throws ScriptException, NoSuchMethodException, IOException {
        return putObject(json, parentExpression, key, value);
    }
    
    public List<String> getStringList(String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        return Arrays.asList(getObject(String[].class, json, expression));
    }
    
    public String putStringList(String json, String parentExpression, String key, List<String> values) throws ScriptException, NoSuchMethodException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonValue = mapper.writeValueAsString(values.toArray(new String[0]));
        return putJsonObject(json, parentExpression, key, jsonValue);
    }
    
    public List<Integer> getIntegerList(String json, String expression) throws ScriptException, NoSuchMethodException, IOException {
        return Arrays.asList(getObject(Integer[].class, json, expression));
    }

    public String putIntegerList(String json, String parentExpression, String key, List<Integer> values) throws ScriptException, NoSuchMethodException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonValue = mapper.writeValueAsString(values.toArray(new Integer[0]));
        return putJsonObject(json, parentExpression, key, jsonValue);
    }

}
