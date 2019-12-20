/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * This class scans the classpath to find possible plugin classes and automatically adds
 * them to the whiteboard
 * 
 * A plugin class can be anything that has a no-arg constructor and is not 
 * itself an interface and, in this case,  has a specific annotation that is
 * being sought out. Implementations will be registered under the annotation
 * class name as the interface/registry-key.
 *
 * @author jbuhacoff
 */
public class AnnotationRegistrar implements Registrar {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnnotationRegistrar.class);
    private final List<Class<? extends Annotation>> acceptable; // when null, will register ANY implementation with ANY annotation; when set, will only register specific annotations

    public AnnotationRegistrar() {
        this.acceptable = null;
    }
    
    
    public AnnotationRegistrar(Class<? extends Annotation> annotation) {
        acceptable = new ArrayList<>();
        acceptable.add(annotation);
    }

    public AnnotationRegistrar(List<String> acceptableAnnotationClassNames) {
        acceptable = new ArrayList<>();
        for(String className : acceptableAnnotationClassNames) {
            // try instantiating the class - if it's not found, skip it
            try {
                Class annotation = Class.forName(className);
                if( annotation.isAnnotation() ) {
                    acceptable.add(annotation);
                }
                else {
                    log.error("Cannot scan for classes annotated with {} because it is not an annotation", className);
                }
            }
            catch(ClassNotFoundException e) {
                log.error("Cannot scan for classes annotated with {} because the annotation class is not found: {}", className, e.getMessage());
            }
        }
    }
    
    // scans given set of classes for classes that are named *Factory and automatically registers them
    @Override
    public boolean accept(Class<?> clazz) {
        boolean accepted = false;
        //  check for any annotations on eligible classes (not interface, not abstract, has zero-arg constructor)
        if( ReflectionUtil.isAnnotatedClass(clazz) ) {
            Annotation[] annotations = clazz.getAnnotations();
            log.debug("Found extension {} with {} annotations", clazz.getSimpleName(), annotations.length);
            for(Annotation annotation : annotations) {
                if( acceptable == null || acceptable.contains(annotation.annotationType()) ) {
                    log.debug("Found extension {} with annotation {}", clazz.getSimpleName(), annotation.annotationType().getName());
                    WhiteboardExtensionProvider.registerAnnotated(annotation.annotationType(), clazz);
                    accepted = true;
                }
            }
        }
        /*
        else {
            // if it's not annotated directly check its parents (until we get to a built-in class)
            Class<?> parent = clazz.getSuperclass();
            if( parent.getName().startsWith("java.") || parent.getName().startsWith("javax.") ) { log.debug("Skipping java. or javax. parent"); return false; }
            log.debug("Scanning parent {}", parent.getName());
            while (!accepted && parent != null && parent != Object.class && parent != clazz) {
                Annotation[] annotations = parent.getAnnotations();
                log.debug("Found extension {} with {} annotations", parent.getSimpleName(), annotations.length);
                for(Annotation annotation : annotations) {
                    if( acceptable == null || acceptable.contains(annotation.annotationType()) ) {
                        log.debug("Found extension {} with annotation {} in parent {}", clazz.getSimpleName(), annotation.annotationType().getName(), parent.getName());
                        WhiteboardExtensionProvider.registerAnnotated(annotation.annotationType(), clazz); // will fail because the clazz is not actually annotated, it's the parent that's annotated; we would have to repeat this parent-searching code in the whiteboard to enable this
                        accepted = true;
                    }
                }
                parent = parent.getSuperclass();
            }
        }
        */
        return accepted;
    }

}
