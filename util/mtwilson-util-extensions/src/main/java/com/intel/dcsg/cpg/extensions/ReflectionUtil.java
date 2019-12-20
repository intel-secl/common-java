/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 *
 * @author jbuhacoff
 */
public class ReflectionUtil {
    // like in ReflectionUtil in cpg-module
    public static boolean hasNoArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasOneArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1) {
                return true;
            }
        }
        return false;
    }
    
    public static Constructor getNoArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return constructor;
            }
        }
        return null;
    }
    
    public static Constructor getOneArgConstructor(Class<?> clazz, Class<?> argument) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if( parameters[0].isAssignableFrom(argument) ) {
                    return constructor;
                }
            }
        }
        return null;
    }

    public static boolean isPluginClass(Class<?> clazz) {
        boolean notInterface = !clazz.isInterface();
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && notAbstract && noArgs;
    }
    public static boolean isContextPluginClass(Class<?> clazz) {
        boolean notInterface = !clazz.isInterface();
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean oneArg = hasOneArgConstructor(clazz);
        return notInterface && notAbstract && oneArg;
    }

    /**
     * 
     * @param clazz
     * @return true if the class has any annotations and is not an interface, not abstract, and has a no-arg constructor
     */
    public static boolean isAnnotatedClass(Class<?> clazz) {
        boolean notInterface = !clazz.isInterface();
        boolean annotated = clazz.getAnnotations().length > 0;
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && notAbstract && annotated && noArgs;
    }

    /**
     * 
     * @param clazz
     * @param annotation
     * @return true if the class has the specified annotation and is not an interface, not abstract, and has a no-arg constructor
     */
    public static boolean isAnnotatedClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        boolean notInterface = !clazz.isInterface();
        boolean annotated = clazz.isAnnotationPresent(annotation);
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && notAbstract && annotated && noArgs;
    }
    
}
