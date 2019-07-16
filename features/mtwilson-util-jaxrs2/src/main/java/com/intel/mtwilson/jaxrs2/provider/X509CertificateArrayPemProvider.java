/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Reference: https://jersey.java.net/documentation/latest/message-body-workers.html
 * 
 * @author jbuhacoff
 */
@Provider
@Consumes({CryptoMediaType.APPLICATION_X_PEM_FILE,MediaType.TEXT_PLAIN})
@Produces({CryptoMediaType.APPLICATION_X_PEM_FILE,MediaType.TEXT_PLAIN})
public class X509CertificateArrayPemProvider implements
      MessageBodyWriter<X509Certificate[]>,
      MessageBodyReader<X509Certificate[]> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return X509Certificate[].class.isAssignableFrom(type) && ( mediaType.toString().equals(CryptoMediaType.APPLICATION_X_PEM_FILE) || mediaType.toString().equals(MediaType.TEXT_PLAIN) );
    }

    @Override
    public long getSize(X509Certificate[] t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(X509Certificate[] t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            String[] pems = new String[t.length];
            for(int i=0; i<pems.length; i++) {
                pems[i] = X509Util.encodePemCertificate(t[i]);
            }
            String out = StringUtils.join(pems, "\n");
            IOUtils.write(out, entityStream);
        }
        catch(CertificateEncodingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return X509Certificate[].class.isAssignableFrom(type) && ( mediaType.toString().equals(CryptoMediaType.APPLICATION_X_PEM_FILE) || mediaType.toString().equals(MediaType.TEXT_PLAIN) );
    }

    @Override
    public X509Certificate[] readFrom(Class<X509Certificate[]> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            String pem = IOUtils.toString(entityStream);
            List<X509Certificate> certs = X509Util.decodePemCertificates(pem);
            X509Certificate[] array = new X509Certificate[certs.size()];
            return certs.toArray(array);
        }
        catch(CertificateException e) {
            throw new IOException(e);
        }
    }
    
}
