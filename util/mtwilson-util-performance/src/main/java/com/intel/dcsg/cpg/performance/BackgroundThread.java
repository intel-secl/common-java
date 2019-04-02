/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.performance;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically invokes any Runnable in a background thread that can be
 * controlled with start and stop commands, with a configurable delay between
 * each run.
 *
 * Note that the delay here is between completing one run() and starting
 * the next run() ... so if the run() time is longer than the delay, the
 * task will NOT be started again... after run() is done, then the clock
 * starts on the delay before run() is called again.
 * 
 * @author jbuhacoff
 */
public class BackgroundThread<T> {

    private Logger log = LoggerFactory.getLogger(getClass());
    private AlarmClock alarm = new AlarmClock(1000, TimeUnit.MILLISECONDS);
    private Runnable task = null;
    private boolean isDone = false;
    private boolean throwExceptions = false;
    private Thread monitorThread = null;
    private String name = null;

    public BackgroundThread() {
    }

    public BackgroundThread(Runnable task) {
        this.task = task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public void setDelay(long duration, TimeUnit unit) {
        alarm.setAlarm(duration, unit);
    }

    public void setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }

    /**
     * Call this before starting the long-running task, in order to start the
     * progress updates.
     */
    public void start() {
        name = task.getClass().getName();
        log.debug("Background thread started for {}", name);
        monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Background thread running: {}", name);
                while (true) {
                    alarm.sleep();
                    // most likely time for controller to call stop() is while
                    // we are sleeping, so check if done before executing
                    if (isDone) {
                        break;
                    }
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("Error while executing background task", e);
                        if (throwExceptions) {
                            isDone = true;
                            throw new RuntimeException(e);
                        }
                    } finally {
                        if (isDone) {
                            break;
                        }
                    }
                }
            }
        }, getName());
        monitorThread.start();
    }

    /**
     * Call this after you are finished, to stop the progress updates.
     */
    public void stop() {
        isDone = true;
        waitfor(monitorThread);
    }

    private void waitfor(Thread thread) {
        boolean done = false;
        log.debug("Waiting for thread {} to finish", thread.getName());
        while (!done) {
            try {
                thread.join();
                done = true;
                log.debug("Thread {} finished", thread.getName());
            } catch (InterruptedException ignored) {
                log.debug("Ignoring interrupt while waiting for {}", thread.getName());
            }
        }
    }

    public String getName() {
        return name;
    }
}
