/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test.model.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.validation.Fault;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
/**
 * Test features of the aspect oriented model validation
 * @author jbuhacoff
 */
public class TestFault {
    private static Logger log = LoggerFactory.getLogger(TestFault.class);
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullDescription() {
        Fault fault = new Fault(null);
        log.debug("should not get here, fault: {}", fault);
        fail("constructor should throw exception with null description");
    }

    @Test
    public void testNullTWithStringArg() {
        Fault fault = new Fault(null, "arg");
        log.debug("fault: {}", fault);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullFormatStringWithIntegerArg() {
        Fault fault = new Fault(null, 5);
        log.debug("should not get here, fault: {}", fault);
        fail("constructor should throw exception with null format");
    }
    
    @Test
    public void testEmptyFormatStringWithStringArg() {
        Fault fault = new Fault("", "arg");
        log.debug("fault: {}", fault);
    }
    
    @Test
    public void testEmptyFormatStringWithIntegerArg() {
        Fault fault = new Fault("", 5);
        log.debug("fault: {}", fault);
    }
    
    @Test
    public void testObjectMapper() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Fault fault = new Fault("test");
        log.debug("fault: {}", mapper.writeValueAsString(fault));
    }
    
}
