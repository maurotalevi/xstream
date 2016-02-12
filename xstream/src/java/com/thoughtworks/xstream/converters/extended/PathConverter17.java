/*
 * Copyright (C) 2016 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 7. February 2016 by Aaron Johnson
 */
package com.thoughtworks.xstream.converters.extended;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * This converter will take care of storing and retrieving a Path.
 *
 * @author Aaron Johnson
 */
public class PathConverter17 extends AbstractSingleValueConverter {
	
    public boolean canConvert(Class type) {
        return Path.class.isAssignableFrom(type);
    }

    public Object fromString(String str) {
    	return Paths.get(str);
    }

    /** The Path.toString() method returns the path as a string already. */
    public String toString(Object obj) {
        return obj + "";
    }
}
