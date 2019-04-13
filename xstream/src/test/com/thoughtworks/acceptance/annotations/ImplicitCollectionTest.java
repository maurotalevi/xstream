/*
 * Copyright (C) 2006, 2007, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 01. December 2006 by Joerg Schaible
 */
package com.thoughtworks.acceptance.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


/**
 * Test for annotations mapping implicit collections.
 *
 * @author Lucio Benfante
 * @author J&ouml;rg Schaible
 */
public class ImplicitCollectionTest extends AbstractAcceptanceTest {

    @Override
    protected XStream createXStream() {
        final XStream xstream = super.createXStream();
        xstream.autodetectAnnotations(true);
        return xstream;
    }

    public void testAnnotation() {
        final String expected = "" //
            + "<root>\n"
            + "  <string>one</string>\n"
            + "  <string>two</string>\n"
            + "</root>";
        final ImplicitRootOne implicitRoot = new ImplicitRootOne();
        implicitRoot.getValues().add("one");
        implicitRoot.getValues().add("two");
        assertBothWays(implicitRoot, expected);
    }

    public void testAnnotationWithItemFieldName() {
        final String expected = "" //
            + "<root>\n"
            + "  <value>one</value>\n"
            + "  <value>two</value>\n"
            + "</root>";
        final ImplicitRootTwo implicitRoot = new ImplicitRootTwo();
        implicitRoot.getValues().add("one");
        implicitRoot.getValues().add("two");
        assertBothWays(implicitRoot, expected);
    }

    public void testAnnotationFailsForInvalidFieldType() {
        try {
            xstream.processAnnotations(InvalidImplicitRoot.class);
            fail("Thrown " + InitializationException.class.getName() + " expected");
        } catch (final InitializationException e) {
            assertTrue(e.getMessage().indexOf("\"value\"") > 0);
        }
    }

    @XStreamAlias("root")
    public static class ImplicitRootOne {
        @XStreamImplicit()
        private List<String> values = new ArrayList<>();

        public List<String> getValues() {
            return values;
        }

        public void setValues(final List<String> values) {
            this.values = values;
        }
    }

    @XStreamAlias("root")
    public static class ImplicitRootTwo {
        @XStreamImplicit(itemFieldName = "value")
        private List<String> values = new ArrayList<>();

        public List<String> getValues() {
            return values;
        }

        public void setValues(final List<String> values) {
            this.values = values;
        }
    }

    @XStreamAlias("root")
    public static class InvalidImplicitRoot {
        @XStreamImplicit(itemFieldName = "outch")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    @XStreamAlias("implicit")
    public static class ImplicitParameterizedType {
        @XStreamImplicit(itemFieldName = "line")
        private ArrayList<ArrayList<Point>> signatureLines;
    }

    @XStreamAlias("point")
    public static class Point {
        @XStreamAsAttribute
        private final int x;
        @XStreamAsAttribute
        private final int y;

        public Point(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void testAnnotationHandlesParameterizedTypes() {
        final String xml = ""
            + "<implicit>\n"
            + "  <line>\n"
            + "    <point x=\"33\" y=\"11\"/>\n"
            + "  </line>\n"
            + "</implicit>";
        final ImplicitParameterizedType root = new ImplicitParameterizedType();
        root.signatureLines = new ArrayList<>();
        root.signatureLines.add(new ArrayList<>());
        root.signatureLines.get(0).add(new Point(33, 11));
        assertBothWays(root, xml);
    }

    @XStreamAlias("type")
    public static class ParametrizedTypeIsInterface {
        @XStreamImplicit()
        private ArrayList<Map<?, ?>> list = new ArrayList<>();
    }

    public void testWorksForTypesThatAreInterfaces() {
        final ParametrizedTypeIsInterface type = new ParametrizedTypeIsInterface();
        type.list = new ArrayList<>();
        type.list.add(new HashMap<>());
        final String xml = "" //
            + "<type>\n"
            + "  <map/>\n"
            + "</type>";
        assertBothWays(type, xml);
    }

    @XStreamAlias("untyped")
    private static class Untyped {
        @SuppressWarnings("rawtypes")
        @XStreamImplicit
        private final List list = new ArrayList();

        @SuppressWarnings("unchecked")
        public Untyped() {
            list.add("1");
        }
    }

    public void testCanHandleUntypedCollections() {
        final Untyped untyped = new Untyped();
        final String xml = "" //
            + "<untyped>\n"
            + "  <string>1</string>\n"
            + "</untyped>";
        assertBothWays(untyped, xml);
    }
}
