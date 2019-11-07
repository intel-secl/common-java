/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.retry;

import com.intel.dcsg.cpg.performance.AlarmClock;

/**
 *
 * @author jbuhacoff
 */
public class Retry {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Retry.class);
    
    public static <T> T limited(Retryable<T> retryable, int maxAttempts, Backoff backoff) throws Exception {
        log.debug("Will make up to {} attempts to execute retryable: {}", maxAttempts, retryable.toString());
        AlarmClock clock = new AlarmClock(0);
        for (int i = 1; i < maxAttempts; i++) {
            try {
                return retryable.call();
            } catch (Exception e) {
                if( !retryable.isRetryable(e)) {
                    throw e;
                }
                log.error("Caught retryable exception", e);
            }
            long delay = backoff.getMilliseconds();
            log.error("Delaying next attempt by {} ms", delay);
            clock.sleep(delay);
        }
        log.debug("Last attempt to execute retryable: {}", retryable.toString());
        // the last attempt, i = maxAttempts
        return retryable.call();
    }
}
