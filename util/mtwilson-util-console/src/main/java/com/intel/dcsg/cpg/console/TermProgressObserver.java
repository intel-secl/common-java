/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console;

import com.intel.dcsg.cpg.performance.*;

/**
 *
 * @author jbuhacoff
 */
public class TermProgressObserver implements Observer<Progress> {
    private Term term = new Term();
    private boolean displayLabel = true;
    private boolean displayCount = true; // default false
    private boolean displayPercent = true;
    private boolean displayBar = true;// default false
    private final Task task;
    public TermProgressObserver() {
        task = null;
    }
    public TermProgressObserver(Task task) {
        this.task = task;
    }
    
    
    @Override
    public void observe(Progress progress) {
        long current = progress.getCurrent();
        long max = progress.getMax();
        String currentText = String.valueOf(current); //current == null ? "unknown" : current.toString();
        String maxText = String.valueOf(max);  //current == null ? "unknown" : max.toString();
        String count = String.format("%s/%s", currentText, maxText); //current == null && max == null ? "unknown" : String.format("%s/%s", currentText, maxText);
        String percent = max == 0 ? "" : String.format("%.0f%%", current*100.0/max); // current == null || max == null || max == 0 ? "" : String.format("%.0f%%", current*100.0/max);
        String labelText = task == null ? progress.getClass().getSimpleName() : task.getId();
        String shortLabelText = labelText.length() > 20 ? labelText.substring(0, 17) + "..." : labelText;
        String label = String.format("%-20s", shortLabelText);
        String barText = generateBar(progress);
        String bar = String.format("[%-20s]", barText);
        String leftSide = (displayLabel?label:"Progress")+(displayLabel && displayBar ? " " : "")+(displayBar?bar:"");
        String rightSide = (displayCount?count:"")+(displayCount && displayPercent ? " " : "")+(displayPercent?percent:"");
        System.out.print(term.clearLine()+leftSide+(!leftSide.isEmpty() && !rightSide.isEmpty() ? " " : "")+rightSide);
    }
    
    private String generateBar(Progress progress) {
        long current = progress.getCurrent();
        long max = progress.getMax();
        if( /* current == null || max == null || */ max == 0 ) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        long length = current*20/max;
        for(long i=0; i<length-1; i++) {
            str.append("=");
        }
        str.append(">");
        return str.toString();
    }
}
