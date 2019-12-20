/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client.retry;

import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.mtwilson.retry.Backoff;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
public class RetryProxy implements InvocationHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryProxy.class);
    private final Object delegate;
    private final Backoff retryBackoff;
    private final Integer retryMax;
    private final AlarmClock alarm;

    private RetryProxy(Object delegate, Backoff retryBackoff, Integer retryMax) {
        this.delegate = delegate;
        this.retryBackoff = retryBackoff;
        this.retryMax = retryMax;
        this.alarm = new AlarmClock();
        if( retryMax != null && retryMax < 0 ) {
            throw new IllegalArgumentException("Parameter 'retryMax' must be null or non-negative");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        int retries = 0;
        while (retryMax == null || retries <= retryMax) {
            try {
                log.debug("retry {} , before delegate invocation, delegate {} , args {}", retries, delegate, args);
                result = method.invoke(delegate, args);
                if (result == null) {
                    return null;
                }
                // if the result object implements javax.ws.rs.Response, then the
                // request was successful (at least from network perspective) and
                // we return the result directly
                if (Response.class.isAssignableFrom(result.getClass())) {
                    log.debug("found response value of class {}", result.getClass().getCanonicalName());
                    return result;
                }
                // if it implements any other interface from javax.ws.rs.client
                // then we need to wrap it with another instance of the proxy.
                // for example, org.glassfish.jersey.client.JerseyWebTarget implements javax.ws.rs.client.WebTarget
                Class<?>[] interfaces = result.getClass().getInterfaces();
                for (Class<?> resultInterface : interfaces) {
                    if (resultInterface.getCanonicalName().startsWith("javax.ws.rs.")) {
                        log.debug("wrapping return value of class {}", result.getClass().getCanonicalName());
                        // result is not a web response but is another jax-rs object,
                        // so wrap it with another proxy instance.
                        return newClientInstance(result, retryBackoff, retryMax);
                    }
                }
                log.debug("after delegate invocation, result class is {}", result.getClass().getCanonicalName());
                return result;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.error("invocation failed", e);
                // not declared by calling code so must wrap in RuntimeException
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                log.debug("call failed ...  now we can retry !!!   backoff={}  max={}", e, retryBackoff, retryMax);
                // we only rethrow the original exception on the last attempt
                if (retryMax != null && retries == retryMax) {
                    throw e.getTargetException();
                }
                retries++;
                if( retryBackoff != null ) {
                    long delay = retryBackoff.getMilliseconds();
                    log.debug("delaying for {}ms before next attempt", delay);
                    alarm.sleep(delay);
                }
            }
        }
        return result;
    }

    public static Object newClientInstance(Object delegate, Backoff retryBackoff, Integer retryMax) {
        return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), delegate.getClass().getInterfaces(), new RetryProxy(delegate, retryBackoff, retryMax));
    }
}
