/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.validation;

import com.intel.mtwilson.util.validation.faults.Thrown;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class strongly resembles an Exception object but it is used differently.
 * 
 * In this validation package, Faults are collected, not thrown. This allows
 * an application to fully validate potentially complex user input, collect
 * all the faults, and then provide
 * complete guidance on any errors that may exist in the input. 
 * 
 * Contrast this to Exceptions, that can only be thrown one at a time and
 * interrupt the flow of execution, forcing the user to correct and resubmit
 * one error at a time until the input is completely validated.
 * 
 * XXX TODO make the Fault class implement Localizable, allow a Message object in a
 * constructor like Fault(Message) and Fault(Throwable,, Message) and then pass the locale to the 
 * Message object when toString() or toString(Locale) is called.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
public class Fault implements Faults {
    private final String description;
    private final List<Fault> more = new ArrayList<>();
    
    public Fault(String description) {
        if( description == null ) { throw new IllegalArgumentException("Cannot create a fault with null description"); }
        this.description = description;
    }
    
    public Fault(String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
        this.description = String.format(format, args);
    }
    
    public Fault(Throwable e, String description) {
        if( description == null ) { throw new IllegalArgumentException("Cannot create a fault with null description"); }
        this.description = description;
        fault(e);
    }
    
    public Fault(Throwable e, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
        this.description = String.format(format, args);
        fault(e);
    }
    
    /**
     * Faults from the given collection are copied to as "more faults" for this one.
     * It is safe to clear or continue using the given collection.
     * @param related faults that may have caused this one
     * @param format
     * @param args 
     */
    public Fault(Collection<Fault> related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
        this.description = String.format(format, args);
        fault(related);
    }

    public Fault(Fault related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
        this.description = String.format(format, args);
        fault(related);
    }
    
    public Fault(Faults related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
        this.description = String.format(format, args);
        fault(related);
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public String toString() {
        return String.format("[%s: %s]", getClass().getName(), description); // should never be null because we set it in every constructor
    }
    
    protected final void fault(Throwable e) {
        more.add(new Thrown(e));
    }

    protected final void fault(Fault fault) {
        more.add(fault);
    }
    
    protected final void fault(Faults faults) {
        more.addAll(faults.getFaults());
    }

    protected final void fault(Collection<Fault> faults) {
        more.addAll(faults);
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * This method returns an array of faults related to this one. Typically
     * these are faults that resulted in the failure described by this fault.
     * 
     * @return a list of related Fault objects; may be empty if there aren't any other associated faults
     */
    @Override
    public List<Fault> getFaults() {
        return more;
    }
    
}
