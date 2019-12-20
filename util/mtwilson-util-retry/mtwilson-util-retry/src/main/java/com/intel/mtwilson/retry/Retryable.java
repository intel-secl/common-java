/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.retry;

import java.io.Closeable;
import java.util.concurrent.Callable;

/**
 * A Retryable object is a Callable that can be called repeatedly and has
 * a method isRetryable that can decide whether an exception thrown by
 * the call() method is one that can be resolved by trying again. It also
 * implements a close() method to clean up any resources that may have been
 * left open between retries, such as network connections.
 * The call() method should open the resources each time.
 * @author jbuhacoff
 */
public interface Retryable<T> extends Callable<T>, Closeable {
    boolean isRetryable(Exception e);
}
