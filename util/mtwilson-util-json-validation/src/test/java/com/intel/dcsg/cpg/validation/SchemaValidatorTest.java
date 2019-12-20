/*
 * Copyright (C) 2016 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.sun.codemodel.JCodeModel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author anjanikx
 */

public class SchemaValidatorTest {
    
    private SchemaValidator schemaValidator;
    public SchemaValidatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        schemaValidator = new SchemaValidator();
        schemaValidator.setSchemaFromPath("src//test//resources//schema//flavor_schema.json");
    }
    
    @After
    public void tearDown() {
        schemaValidator = null;
    }

    
    @Test
    public void testSchemaValidatorWithGoodJson1() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithGoodJson2() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good1.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithGoodJson3() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good2.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithGoodJson4() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good4.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithGoodJson5() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good5.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson1() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad1.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson2() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad2.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson3() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad3.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson4() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad4.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson5() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad5.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson6() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad6.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson7() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad7.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson8() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad8.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson9() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad9.json");
        assertFalse(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithBadJson10() throws IOException {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad10.json");
        assertFalse(schemaValidator.isValid());
    }
    
    
    //rest examples validating using jsonpath
    @Test(expected = PathNotFoundException.class)
    public void testPathExists1() throws Exception {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-bad10.json");
        JsonNode jsonData = schemaValidator.getJSONData();
        Configuration configuration = Configuration.defaultConfiguration();
        Object data = JsonPath.parse(jsonData.toString(), configuration).read("software.vendor");
        assertTrue(data == null);
    }
    
    @Test
    public void testPathExists2() throws Exception {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good.json");
        JsonNode jsonData = schemaValidator.getJSONData();
        Configuration configuration = Configuration.defaultConfiguration();
        List<String> data = JsonPath.parse(jsonData.toString(), configuration).read("$..hardware.feature.TPM[?(@.enabled == true)].version");
        assertTrue(data != null && data.contains("1.2") || data.contains("2"));
    }
    
    @Test
    public void testPathExists3() throws Exception {
        schemaValidator.setDocumentFromPath("src//test//resources//sample//flavor-good.json");
        JsonNode jsonData = schemaValidator.getJSONData();
        JCodeModel codeModel = new JCodeModel();



GenerationConfig config = new DefaultGenerationConfig() {
@Override
public boolean isGenerateBuilders() { // set config option by overriding method
return true;
}
};

SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(), new SchemaStore()), new SchemaGenerator());
mapper.generate(codeModel, "TrustFlavor", "com.intel", schemaValidator.getJSONSchema().toString());

codeModel.build(new File("src//test//resources//sample//output"));

        Configuration configuration = Configuration.defaultConfiguration();
        List<String> data = JsonPath.parse(jsonData.toString(), configuration).read("$..software.measurement['/etc/hosts'].digest");
        assertTrue(data != null && data.contains("2cab4cce4d11a47acdd4625b63f207974824d1d7f1804f4d4825f7a4472d39f9"));
    }

    @Test
    public void testSchemaValidatorWithPolicyGoodJson() throws IOException {
        schemaValidator.setSchemaFromPath("src//test//resources//schema//policy_schema.json");
        schemaValidator.setDocumentFromPath("src//test//resources//sample//policy-good.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithPolicyGoodJson1() throws IOException {
        schemaValidator.setSchemaFromPath("src//test//resources//schema//policy_schema.json");
        schemaValidator.setDocumentFromPath("src//test//resources//sample//policy_valid.json");
        assertTrue(schemaValidator.isValid());
    }
    
    @Test
    public void testSchemaValidatorWithPolicyBadJson() throws IOException {
        schemaValidator.setSchemaFromPath("src//test//resources//schema//policy_schema.json");
        schemaValidator.setDocumentFromPath("src//test//resources//sample//policy-bad.json");
        assertFalse(schemaValidator.isValid());
    }
}