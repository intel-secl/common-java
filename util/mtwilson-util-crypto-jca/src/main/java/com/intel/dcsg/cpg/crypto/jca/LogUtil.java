/*
 * Copyright 2016 Intel Corporation. All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.jca;

/**
 * To remove logging dependencies:
 *
 * 1. Disable SLF4J import statements:
 *
 * <pre>
 * //import org.slf4j.Logger;
 * //import org.slf4j.LoggerFactory;
 * </pre>
 *
 * 2. Replace SLF4J declaration with LogUtil:
 *
 * <pre>
 * //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Example.class);
 * private static final LogUtil.Logger log = new LogUtil.Logger();
 * </pre>
 *
 * @author jbuhacoff
 */
public class LogUtil {

    /*
    public static Logger getLogger(String label) {
        return org.slf4j.LoggerFactory.getLogger(label);
//        return new Logger();
    }
    public static Logger getLogger(Class label) {
        return org.slf4j.LoggerFactory.getLogger(label);
//        return new Logger();
    }
     */

    /**
     * A minimalist implementation of no-op logging methods intended to be a
     * drop-in replacement for most commonly used methods of `org.slf4j.Logger`.
     * The `Marker` methods are not available.
     */
    public static class Logger {

        public boolean isErrorEnabled() {
            return false;
        }

        public boolean isWarnEnabled() {
            return false;
        }

        public boolean isInfoEnabled() {
            return false;
        }

        public boolean isDebugEnabled() {
            return false;
        }

        public boolean isTraceEnabled() {
            return false;
        }

        public void error(String message, Object... details) {
        }

        public void warn(String message, Object... details) {
        }

        public void info(String message, Object... details) {
        }

        public void debug(String message, Object... details) {
        }

        public void trace(String message, Object... details) {
        }
    }
}
