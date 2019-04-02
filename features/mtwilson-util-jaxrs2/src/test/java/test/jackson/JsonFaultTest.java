/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package test.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.util.ArrayList;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class JsonFaultTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonFaultTest.class);

    public static class Bean  {
        public String getName() { return "testbean"; }
        public List<Fault> getFaults() {
            ArrayList<Fault> faults = new ArrayList<>();
            faults.add(new Fault1("setting1", "withArgument"));
            faults.add(new Fault1("setting2", null));
            return faults;
        }
    }
    
    public static class Fault1 extends Fault {
        private String arg;
        public Fault1(String description, String arg) {
            super(description);
            this.arg = arg;
        }

        public String getArg() {
            return arg;
        }
    }
    
    @Test
    public void testSerializeFaultsDefaultMapper() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        log.debug("default mapper bean: {}", mapper.writeValueAsString(new Bean()));
        /**
         * Default object mapper produces empty "faults" field because getFaults()  never returns null
         * 
         * default mapper bean: {"name":"testbean","faults":[{"description":"setting1","arg":"withArgument","faults":[]},{"description":"setting2","arg":null,"faults":[]}]}
         */
    }
    @Test
    public void testSerializeFaultsCustomMapper() throws JsonProcessingException {
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        log.debug("custom mapper bean: {}", mapper.writeValueAsString(new Bean()));
        /**
         * Custom mapper omits the empty "faults" field and adds a "type" field to indicate the fault class
         * 
         * custom mapper bean: {"name":"testbean","faults":[{"type":"test.jackson.JsonFaultTest$Fault1","description":"setting1","arg":"withArgument"},{"type":"test.jackson.JsonFaultTest$Fault1","description":"setting2"}]}
         */
    }
}
