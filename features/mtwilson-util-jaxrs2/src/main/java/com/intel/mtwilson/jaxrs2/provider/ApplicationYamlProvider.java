/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author jbuhacoff
 */
@Provider
@Consumes({"application/yaml","text/yaml"})
@Produces({"application/yaml","text/yaml"})
public class ApplicationYamlProvider implements
      MessageBodyWriter<Object>,
      MessageBodyReader<Object> {

    private JacksonYamlObjectMapperProvider jackson = new JacksonYamlObjectMapperProvider();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ( mediaType.toString().equals("application/yaml") || mediaType.toString().equals("text/yaml") );
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        ObjectMapper mapper = jackson.getContext(type);
        mapper.writeValue(entityStream, t);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ( mediaType.toString().equals("application/yaml") || mediaType.toString().equals("text/yaml") );
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        ObjectMapper mapper = jackson.getContext(type);
        return mapper.readValue(entityStream, type);
    }
    
}
