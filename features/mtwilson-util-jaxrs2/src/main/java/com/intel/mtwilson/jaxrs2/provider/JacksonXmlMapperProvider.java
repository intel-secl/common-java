/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.jaxrs2.XmlAnnotationIntrospector;

/**
 * A hypothetical example JSON output using the default ObjectMapper settings 
 * might look like this (notice camelCase on connectionUrl and biosMLE):
 * 

<host_collection><hosts><host><id>623b6ebc-e6b5-4eef-a466-75d03bd12a06</id><name>hostabc</name><connectionUrl>http://1.2.3.4</connectionUrl><description>test host</description><biosMLE>bios-4.3.2</biosMLE></host></hosts></host_collection>
* 
 * 
 * But the same example object when this provider is on the classpath will
 * look like this (notice the underscores connection_url and bios_mle):
 * 

<host_collection><hosts><host><id>8d4f8fbc-b7d8-4827-9aa6-44df82ffb62f</id><name>hostabc</name><connection_url>http://1.2.3.4</connection_url><description>test host</description><bios_mle>bios-4.3.2</bios_mle></host></hosts></host_collection>
 * 
 * References:
 * 
 * https://github.com/FasterXML/jackson-dataformat-xml/wiki/Jackson-XML-annotations
 * http://stackoverflow.com/questions/14712312/how-to-serialize-java-object-as-xml-attribute-with-jackson
 * http://stackoverflow.com/questions/12904250/jackson-xml-globally-set-element-name-for-container-types
 * 
 * 
 * @author jbuhacoff
 */
@Provider
@Produces({MediaType.APPLICATION_XML,MediaType.TEXT_XML})
public class JacksonXmlMapperProvider implements ContextResolver<XmlMapper> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonXmlMapperProvider.class);

    private final XmlMapper xmlMapper;
 
    public JacksonXmlMapperProvider() {
        log.trace("JacksonXmlMapperProvider constructor");
        xmlMapper = createDefaultMapper();
    }
 
    @Override
    public XmlMapper getContext(Class<?> type) {
        log.trace("JacksonXmlMapperProvider getContext");
        return xmlMapper;
    }
 
    private XmlMapper createDefaultMapper() {
        log.trace("JacksonXmlMapperProvider createDefaultMapper");
        XmlMapper mapper = new XmlMapper(/*jsonFactory*/);
        // fix for ISECL-8791 - custom annotation for field only marshalled in JSON
        mapper.setAnnotationIntrospector(new XmlAnnotationIntrospector());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        List<Module> jacksonModules = Extensions.findAll(Module.class);
        for(Module module : jacksonModules) {
            log.trace("JacksonXmlMapperProvider registering module: {} class: {}", module.getModuleName(), module.getClass().getName());
            mapper.registerModule(module); // for example com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule, com.intel.mtwilson.jackson.validation.ValidationModule, com.intel.mtwilson.jackson.v2api.V2Module
        }
        return mapper;
    }
 
}    
