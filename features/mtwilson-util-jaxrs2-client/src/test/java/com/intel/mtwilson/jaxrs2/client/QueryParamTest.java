/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.QueryParam;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class QueryParamTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QueryParamTest.class);
    
    @Test
    public void testMapQueryParam() throws MalformedURLException {
        QueryParamBean bean = new QueryParamBean();
        bean.ext.put("baz", "quux");
        bean.ext.put("magic", "xyzzy");
        JaxrsClient client = JaxrsClientBuilder.factory().url(new URL("http://intel.com")).tlsPolicy(new InsecureTlsPolicy()).build();
        log.debug("uri: {}", client.getTargetPathWithQueryParams("/path", bean).getUri().toString());
        // example output: http://localhost/path?magic=xyzzy&baz=quux&foo=bar
    }
    @Test
    public void testMapQueryParamWithPrefix() throws MalformedURLException {
        QueryParamBeanWithMapPrefix bean = new QueryParamBeanWithMapPrefix();
        bean.ext.put("baz", "quux");
        bean.ext.put("magic", "xyzzy");
        JaxrsClient client = JaxrsClientBuilder.factory().url(new URL("http://localhost")).tlsPolicy(new InsecureTlsPolicy()).build();
        log.debug("uri: {}", client.getTargetPathWithQueryParams("/path", bean).getUri().toString());
        // example output: http://localhost/path?my.magic=xyzzy&my.baz=quux&foo=bar
    }
    
    public static class QueryParamBean {
        @QueryParam("foo")
        public String foo = "bar";
        
        @QueryParam("")
        public Map<String,String> ext = new HashMap<>();
    }

    public static class QueryParamBeanWithMapPrefix {
        @QueryParam("foo")
        public String foo = "bar";
        
        @QueryParam("my.")
        public Map<String,String> ext = new HashMap<>();
    }

}
