/*
 * Copyright (C) 2020, 2021 XStream committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 19. August 2020 by Julia Boes
 */

package com.thoughtworks.xstream.converters.extended;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import static java.lang.invoke.MethodType.methodType;


/**
 * Converts a Record.
 *
 * @author <a href="mailto:julia.boes@oracle.com">Julia Boes</a>
 * @author <a href="mailto:chris.hegarty@oracle.com">Chris Hegarty</a>
 */
public final class RecordConverter implements Converter {
    private static final MethodHandle MH_IS_RECORD;
    private static final MethodHandle MH_GET_RECORD_COMPONENTS;
    private static final MethodHandle MH_GET_NAME;
    private static final MethodHandle MH_GET_TYPE;
    private static final MethodHandles.Lookup LOOKUP;

    protected final Mapper mapper;

    public RecordConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    static {
        MethodHandle MH_isRecord;
        MethodHandle MH_getRecordComponents;
        MethodHandle MH_getName;
        MethodHandle MH_getType;
        LOOKUP = MethodHandles.lookup();

        try {
            // reflective machinery required to access the record components
            // without a static dependency on Java SE 16 APIs or Java SE 14 or 15 API with preview enabled
            Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
            MH_isRecord = LOOKUP.findVirtual(Class.class, "isRecord", methodType(boolean.class));
            MH_getRecordComponents = LOOKUP.findVirtual(Class.class, "getRecordComponents", methodType(Array
                .newInstance(c, 0).getClass())).asType(methodType(Object[].class, Class.class));
            MH_getName = LOOKUP.findVirtual(c, "getName", methodType(String.class)).asType(methodType(String.class,
                Object.class));
            MH_getType = LOOKUP.findVirtual(c, "getType", methodType(Class.class)).asType(methodType(Class.class,
                Object.class));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // pre-Java-14
            MH_isRecord = null;
            MH_getRecordComponents = null;
            MH_getName = null;
            MH_getType = null;
        } catch (IllegalAccessException unexpected) {
            throw new AssertionError(unexpected);
        }

        MH_IS_RECORD = MH_isRecord;
        MH_GET_RECORD_COMPONENTS = MH_getRecordComponents;
        MH_GET_NAME = MH_getName;
        MH_GET_TYPE = MH_getType;
    }

    /** Returns true if, and only if, the given class is a record class. */
    private static boolean isRecord(Class<?> aClass) {
        try {
            return (boolean)MH_IS_RECORD.invokeExact(aClass);
        } catch (Throwable t) {
            throw new ConversionException(t);
        }
    }

    /** A record component, which as a name and a type. */
    final static class RecordComponent {
        private final String name;
        private final Class<?> type;

        RecordComponent(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        String name() {
            return name;
        }

        Class<?> type() {
            return type;
        }
    }

    /** Converts record classes only. All record classes. */
    @Override
    public boolean canConvert(Class<?> type) {
        return isRecord(type);
    }

    /** Marshals a record. */
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Class<?> cls = source.getClass();
        if (!isRecord(cls)) {
            throw new ConversionException(source + " is not a record");
        }
        Arrays.stream(recordComponents(cls)).forEach(rc -> writeItem(rc, componentValue(source, rc), context, writer));
    }

    private void writeItem(RecordComponent recordComponent, Object compValue, MarshallingContext context,
            HierarchicalStreamWriter writer) {
        if (compValue == null) {
            return; // omit null values
        } else {
            writer.startNode(recordComponent.name(), compValue.getClass());
            final Class<?> defaultType = mapper.defaultImplementationOf(recordComponent.type());
            final Class<?> actualType = compValue.getClass();
            final String attributeName = mapper.aliasForSystemAttribute("class");
            final String serializedClassName = mapper.serializedClass(actualType);
            if (!actualType.equals(defaultType)) {
                if (!serializedClassName.equals(mapper.serializedClass(defaultType))) {
                    writer.addAttribute(attributeName, serializedClassName);
                }
            } else if (isRecord(actualType)) {
                // always add class attribute for nested records
                writer.addAttribute(attributeName, serializedClassName);
            }
            context.convertAnother(compValue);
            writer.endNode();
        }
    }

    /**
     * Unmarshals a record. Reconstitutes all stream values first (if any), then matches the stream value to the
     * corresponding component parameter position of the record's canonical constructor, lastly, invokes the canonical
     * constructor.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Class<?> aRecord = context.getRequiredType();
        if (!isRecord(aRecord)) {
            throw new ConversionException(aRecord + " is not a record");
        }
        final RecordComponent[] recordComponents = recordComponents(aRecord);
        final HashMap<String, Object> valueMap = new HashMap<>();
        while (reader.hasMoreChildren()) {
            Class<?> c;
            reader.moveDown();
            String name = reader.getNodeName();
            if (reader.getAttribute("class") != null) {
                c = classForName(reader.getAttribute("class"));
            } else {
                c = Arrays.stream(recordComponents).filter(rc -> rc.name().equals(name)).map(RecordComponent::type)
                    .findFirst().orElseThrow(() -> new ConversionException("Type not found for " + name));
            }
            valueMap.put(name, context.convertAnother(null, c));
            reader.moveUp();
        }
        Object[] args = orderValues(valueMap, recordComponents);
        return invokeCanonicalConstructor(aRecord, recordComponents, args);
    }

    /**
     * Returns an ordered array of the record components for the given record class. The order is that of the components
     * in the record attribute of the class file.
     */
    private static RecordComponent[] recordComponents(Class<?> cls) {
        try {
            Object[] rawComponents = (Object[])MH_GET_RECORD_COMPONENTS.invokeExact(cls);
            RecordComponent[] recordComponents = new RecordComponent[rawComponents.length];
            for (int i = 0; i < rawComponents.length; i++) {
                final Object comp = rawComponents[i];
                recordComponents[i] = new RecordComponent((String)MH_GET_NAME.invokeExact(comp), (Class<?>)MH_GET_TYPE
                    .invokeExact(comp));
            }
            return recordComponents;
        } catch (Throwable t) {
            throw new ConversionException("cannot retrieve record components", t);
        }
    }

    /** Retrieves the value of the record component for the given record object. */
    private static Object componentValue(Object recordObject, RecordComponent recordComponent) {
        try {
            MethodHandle MH_get = LOOKUP.findVirtual(recordObject.getClass(), recordComponent.name(), methodType(
                recordComponent.type()));
            return MH_get.invoke(recordObject);
        } catch (Throwable t) {
            throw new ConversionException("cannot retrieve record components", t);
        }
    }

    /**
     * Returns a array containing values ordered by that of the record components. Individual values are retrieved
     * (matched by name) from the given valueMap. This order follows that of the record components, which also matches
     * the order of parameters of the canonical constructor. Where no matching value is found, the default value of the
     * component type is inserted.
     */
    private static Object[] orderValues(HashMap<String, Object> valueMap, RecordComponent[] recordComponents) {
        return Arrays.stream(recordComponents).map(rc -> valueMap.getOrDefault(rc.name(), defaultValueFor(rc.type())))
            .toArray(Object[]::new);
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConversionException("Class not found.", e);
        }
    }

    /**
     * Invokes the canonical constructor of a record class with the given argument values.
     */
    private static Object invokeCanonicalConstructor(Class<?> recordClass, RecordComponent[] recordComponents,
            Object[] args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(recordComponents).map(RecordComponent::type).toArray(Class<?>[]::new);
            MethodHandle MH_canonicalConstructor = LOOKUP.findConstructor(recordClass, methodType(void.class,
                paramTypes)).asType(methodType(Object.class, paramTypes));
            return MH_canonicalConstructor.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new ConversionException("Cannot construct type", t);
        }
    }

    /** Returns the default value for the given type. */
    private static Object defaultValueFor(Class<?> type) {
        if (type == Integer.TYPE)
            return 0;
        else if (type == Byte.TYPE)
            return (byte)0;
        else if (type == Long.TYPE)
            return 0L;
        else if (type == Float.TYPE)
            return 0.0f;
        else if (type == Double.TYPE)
            return 0.0d;
        else if (type == Short.TYPE)
            return (short)0;
        else if (type == Character.TYPE)
            return '\u0000';
        else if (type == Boolean.TYPE)
            return false;
        else
            return null;
    }
}
