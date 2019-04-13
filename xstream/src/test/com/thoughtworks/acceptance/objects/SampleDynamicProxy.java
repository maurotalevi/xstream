/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2010 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 25. April 2004 by Joe Walnes
 */
package com.thoughtworks.acceptance.objects;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class SampleDynamicProxy implements InvocationHandler {

    private final Object aField;
    private transient boolean recursion;

    private SampleDynamicProxy(final Object value) {
        aField = value;
    }

    public static interface InterfaceOne {
        Object doSomething();
    }

    public static interface InterfaceTwo {
        Object doSomething();
    }

    public static Object newInstance() {
        return newInstance("hello");
    }

    public static Object newInstance(final Object value) {
        return Proxy.newProxyInstance(InterfaceOne.class.getClassLoader(), new Class[]{
            InterfaceOne.class, InterfaceTwo.class}, new SampleDynamicProxy(value));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	switch (method.getName()) {
	    case "equals":
		return recursion || equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
	    case "hashCode":
		return new Integer(System.identityHashCode(proxy));
	    default:
		return aField;
	}
    }

    @Override
    public boolean equals(final Object obj) {
        try {
            recursion = true;
            return equalsInterfaceOne(obj) && equalsInterfaceTwo(obj);
        } finally {
            recursion = false;
        }
    }

    private boolean equalsInterfaceOne(final Object o) {
        if (o instanceof InterfaceOne) {
            final InterfaceOne interfaceOne = (InterfaceOne)o;
            return aField.equals(interfaceOne.doSomething());
        } else {
            return false;
        }
    }

    private boolean equalsInterfaceTwo(final Object o) {
        if (o instanceof InterfaceTwo) {
            final InterfaceTwo interfaceTwo = (InterfaceTwo)o;
            return aField.equals(interfaceTwo.doSomething());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return aField.hashCode();
    }
}
