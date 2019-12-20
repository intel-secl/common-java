/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/**
 *
 * @author anjanikx
 */
public class SchemaValidator {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaValidator.class);
    private JsonNode jsonData; //to hold json data
    private JsonNode jsonSchema; //to hold json schema

    /*to read jsonData from file
        *Same as fromPath(String), but this time the user supplies the File object instead
    */
    public void setDocumentFromFile(File file) throws IOException {
        this.jsonData = JsonLoader.fromFile(file);
    }

    

    /*
    Read a JsonNode from a file on the local filesystem
    */
    public void setDocumentFromPath(String path) throws IOException {
        this.jsonData = JsonLoader.fromPath(path);
    }

    /*
    Read a JsonNode from a string input
    */
    public void setDocumentFromString(String jsonStr) throws IOException {
        this.jsonData = JsonLoader.fromString(jsonStr);
    }

    /*
    Read a JsonNode from an URL
    */
    public void setDocumentFromURL(URL url) throws IOException {
        this.jsonData = JsonLoader.fromURL(url);
    }

    /*
    Read a JsonNode from a user supplied Reader
    */
    public void setDocumentFromReader(Reader reader) throws IOException {
        this.jsonData = JsonLoader.fromReader(reader);
    }

    public JsonNode getJSONData() {
        return this.jsonData;
    }

    public JsonNode getJSONSchema() {
        return this.jsonSchema;
    }

    /*to read jsonSchema from file
        *Same as fromPath(String), but this time the user supplies the File object instead
    */
    public void setSchemaFromFile(File file) throws IOException {
        this.jsonSchema = JsonLoader.fromFile(file);
    }

    

    /*
    Read a JsonNode from a file on the local filesystem
    */
    public void setSchemaFromPath(String path) throws IOException {
        this.jsonSchema = JsonLoader.fromPath(path);
    }

    /*
    Read a JsonNode from a string input
    */
    public void setSchemaFromString(String jsonStr) throws IOException {
        this.jsonSchema = JsonLoader.fromString(jsonStr);
    }

    /*
    Read a JsonNode from an URL
    */
    public void setSchemaFromURL(URL url) throws IOException {
        this.jsonSchema = JsonLoader.fromURL(url);
    }

    /*
    Read a JsonNode from a user supplied Reader
    */
    public void setSchemaFromReader(Reader reader) throws IOException {
        this.jsonSchema = JsonLoader.fromReader(reader);
    }

   
    /*
    Validate json document against a schema, returns true if json is in accordance with schema
    */
    public boolean isValid() {
        
        try {
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

            JsonSchema schema = null;
            try {
                schema = factory.getJsonSchema(this.jsonSchema);
            } catch (ProcessingException ex) {
                log.error("Invalid JSON");
                return false;
            }

            ProcessingReport report = schema.validate(this.jsonData);
            return report.isSuccess();
        } catch (ProcessingException ex) {
            log.error("Invalid JSON");
            
        }
        return false;
    }
    
}

