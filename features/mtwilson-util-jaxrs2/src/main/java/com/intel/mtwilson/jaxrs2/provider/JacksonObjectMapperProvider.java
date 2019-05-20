/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * A hypothetical example JSON output using the default ObjectMapper settings 
 * might look like this (notice camelCase on connectionUrl and biosMLE):
 * 
{"hosts":[{"id":"093b4663-ca45-4d3e-8a3a-f1f584996a6b","name":"hostabc","connectionUrl":"http://1.2.3.4","description":"test host","biosMLE":"bios-4.3.2"}]}
 * 
 * But the same example object when this provider is on the classpath will
 * look like this (notice the underscores connection_url and bios_mle):
 * 
{"hosts":[{"id":"093b4663-ca45-4d3e-8a3a-f1f584996a6b","name":"hostabc","connection_url":"http://1.2.3.4","description":"test host","bios_mle":"bios-4.3.2"}]}
 * 
 * 
 * @author jbuhacoff
 */
@Provider
@Produces({MediaType.APPLICATION_JSON,DataMediaType.APPLICATION_RELATIONAL_PATCH_JSON,DataMediaType.APPLICATION_VND_API_JSON,DataMediaType.APPLICATION_JSON_PATCH})
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonObjectMapperProvider.class);

    private final ObjectMapper defaultObjectMapper;
 
    public JacksonObjectMapperProvider() {
        log.trace("JacksonObjectMapperProvider constructor");
        defaultObjectMapper = createDefaultMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        log.trace("JacksonObjectMapperProvider getContext");
        return defaultObjectMapper;
    }
 
    public static ObjectMapper createDefaultMapper() {
        log.trace("JacksonObjectMapperProvider createDefaultMapper");
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        List<Module> jacksonModules = Extensions.findAll(Module.class);
        for(Module module : jacksonModules) {
            log.trace("JacksonObjectMapperProvider registering module: {} class: {}", module.getModuleName(), module.getClass().getName());
            mapper.registerModule(module); // for example com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule, com.intel.mtwilson.jackson.validation.ValidationModule, com.intel.mtwilson.jackson.v2api.V2Module
        }
        return mapper;
    }
 
}    
