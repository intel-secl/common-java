/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class RunnableCollection implements Runnable, Progress {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunnableCollection.class);

    protected Collection<Runnable> tasks = null;
    protected long completed = 0; // completed steps not including the current executing task
    protected long max = 0;
    protected ArrayList<Throwable> errors = new ArrayList<>();

    public RunnableCollection(Collection<Runnable> tasks) {
        this.tasks = tasks;
    }
    
    @Override
    public void run() {
        errors.clear();
        completed = 0;
        max = tasks.size();
        for(Runnable task : tasks) {
            try {
                task.run();
                completed++;
                log.debug("Completed {} of {} tasks", completed, max);
            }
            catch(Throwable e) {
                log.error("Execution error {}: {}", e.getClass().getName(), e.getMessage());
                log.debug("Execution error", e);
                errors.add(e);
            }
        }
    }

    @Override
    public long getCurrent() {
        return completed;
    }

    @Override
    public long getMax() {
        return max;
    }
    
    public boolean isError() { return !errors.isEmpty(); }
    public List<Throwable> getErrors() { return errors; }
}
