/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.collection.MultivaluedHashMap;
import com.intel.mtwilson.v2.rpc.RpcUtil;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Code shared by AsyncRpc and BlockingRpc, mostly converting the POST body
 * from its original format to xstream for storing in database.
 *
 * @author jbuhacoff
 */
public class AbstractRpc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractRpc.class);

    protected RpcRepository repository = new RpcRepository();

    protected RpcRepository getRepository() { return repository; }

    @Context
    protected MessageBodyWorkers workers;

    protected MessageBodyWorkers getMessageBodyWorkers() { return workers; }

    protected XStream xstream = new XStream();

    protected RpcAdapter getAdapter(String name) {
        RpcAdapter adapter = RpcUtil.findRpcForName(name);
        if (adapter == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return adapter;
    }

    protected Object getInput(byte[] input, Class<?> inputClass, HttpServletRequest request) {
        Object inputObject;
        // convert the client's input into our internal format
        try {
            String inputAccept = RpcUtil.getPreferredTypeFromAccept(request.getHeader(HttpHeaders.CONTENT_TYPE));
            log.debug("Client prefers content type: {}", inputAccept);
            MediaType inputMediaType = MediaType.valueOf(inputAccept);

            // use jersey classes to find the appropriate message body reader based on request's content type 
            final MessageBodyReader messageBodyReader =
                    workers.getMessageBodyReader(inputClass, inputClass,
                            inputClass.getAnnotations(), inputMediaType);
            log.debug("MessageBodyReader class : {}", messageBodyReader.getClass());
            if (messageBodyReader == null) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
            javax.ws.rs.core.MultivaluedHashMap jaxrsHeaders = new javax.ws.rs.core.MultivaluedHashMap();
            MultivaluedHashMap<String, String> headerMap = RpcUtil.convertHeadersToMultivaluedMap(request);
            jaxrsHeaders.putAll(headerMap.getMap());
            if ((inputAccept.toLowerCase().contains("xml") &&
                    rpcXMLInputValid(inputClass, new ByteArrayInputStream(input))) ||
                    (inputAccept.toLowerCase().contains("json") &&
                            rpcJSONInputValid(inputClass, new ByteArrayInputStream(input)))) {
                inputObject = messageBodyReader.readFrom(inputClass, inputClass, inputClass.getAnnotations(), inputMediaType, jaxrsHeaders, new ByteArrayInputStream(input));
            } else {
                log.error("Input provided to RPC request is incorrect");
                throw new IOException("Input provided to RPC request is incorrect");
            }

        } catch (IOException e) {
            log.error ("IOException.", e);
            throw new WebApplicationException("Invalid input to RPC", e);
        }
        return inputObject;
    }


    protected byte[] toXml(Object inputObject) {
        return xstream.toXML(inputObject).getBytes(Charset.forName("UTF-8"));
    }

    private boolean rpcXMLInputValid(Class<?> inputClass, ByteArrayInputStream input) {
        String defaultXmlBindFactory = System.getProperty("javax.xml.bind.context.factory");
        try {
            System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
            JAXBContext jc = JAXBContext.newInstance(inputClass);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/xml");
            unmarshaller.setEventHandler(new CustomValidationEventHandler());
            unmarshaller.unmarshal(new StreamSource(input), inputClass);
        } catch (JAXBException ex) {
            log.error("Error unmarshalling input to input class {}", inputClass, ex);
            return false;
        }
        finally {
            System.setProperty("javax.xml.bind.context.factory", defaultXmlBindFactory);
        }
        return true;
    }

    private boolean rpcJSONInputValid(Class<?> inputClass, ByteArrayInputStream input) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            mapper.readValue(input, inputClass);
        } catch (IOException ex) {
            log.error("Error unmarshalling input to input class {}", inputClass, ex);
            return false;
        }
        return true;
    }
}
