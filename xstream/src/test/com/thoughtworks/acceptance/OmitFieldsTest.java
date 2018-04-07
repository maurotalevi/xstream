/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2010, 2012, 2013, 2014, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 20. June 2005 by Joe Walnes
 */
package com.thoughtworks.acceptance;

import com.thoughtworks.acceptance.objects.StandardObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;


public class OmitFieldsTest extends AbstractAcceptanceTest {

    public static class Thing extends StandardObject {
        private static final long serialVersionUID = 200506L;
        transient String alwaysIgnore;
        String sometimesIgnore;
        String neverIgnore;
    }

    public void testTransientFieldsAreOmittedByDefault() {
        final Thing in = new Thing();
        in.alwaysIgnore = "a";
        in.sometimesIgnore = "b";
        in.neverIgnore = "c";

        final String expectedXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final Thing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testAdditionalFieldsCanBeExplicitlyOmitted() {
        final Thing in = new Thing();
        in.alwaysIgnore = "a";
        in.sometimesIgnore = "b";
        in.neverIgnore = "c";

        final String expectedXml = "" //
            + "<thing>\n" 
            + "  <neverIgnore>c</neverIgnore>\n" 
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "sometimesIgnore");

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final Thing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals(null, out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public static class DerivedThing extends Thing {
        private static final long serialVersionUID = 200603L;
        String derived;
    }

    public void testInheritedFieldsCanBeExplicitlyOmittedThroughFacade() {
        final DerivedThing in = new DerivedThing();
        in.alwaysIgnore = "a";
        in.sometimesIgnore = "b";
        in.neverIgnore = "c";
        in.derived = "d";

        final String expectedXml = ""
            + "<thing>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "  <derived>d</derived>\n"
            + "</thing>";

        xstream.alias("thing", DerivedThing.class);
        xstream.omitField(Thing.class, "sometimesIgnore");

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final DerivedThing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals(null, out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
        assertEquals("d", out.derived);
    }

    public void testFieldsOfDerivedTypesCanBeExplicitlyOmittedThroughFacade() {
        final DerivedThing in = new DerivedThing();
        in.alwaysIgnore = "a";
        in.sometimesIgnore = "b";
        in.neverIgnore = "c";
        in.derived = "d";

        final String expectedXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", DerivedThing.class);
        xstream.omitField(DerivedThing.class, "derived");

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final DerivedThing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
        assertEquals(null, out.derived);
    }

    public static class AnotherThing extends StandardObject {
        private static final long serialVersionUID = 200506L;
        String stuff;
        String cheese;
        String myStuff;
        String myCheese;
    }

    public void testFieldsCanBeIgnoredUsingCustomStrategy() {
        final AnotherThing in = new AnotherThing();
        in.stuff = "a";
        in.cheese = "b";
        in.myStuff = "c";
        in.myCheese = "d";

        final String expectedXml = "" //
                + "<thing>\n" + "  <stuff>a</stuff>\n" + "  <cheese>b</cheese>\n" + "</thing>";

        class OmitFieldsWithMyPrefixMapper extends MapperWrapper {
            public OmitFieldsWithMyPrefixMapper(final Mapper wrapped) {
                super(wrapped);
            }

            @Override
            public boolean shouldSerializeMember(final Class<?> definedIn, final String fieldName) {
                return !fieldName.startsWith("my");
            }
        }

        xstream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(final MapperWrapper next) {
                return new OmitFieldsWithMyPrefixMapper(next);
            }
        };

        xstream.allowTypes(AnotherThing.class);
        xstream.alias("thing", AnotherThing.class);

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final AnotherThing out =xstream.fromXML(actualXml);
        assertEquals("a", out.stuff);
        assertEquals("b", out.cheese);
        assertEquals(null, out.myStuff);
        assertEquals(null, out.myCheese);
    }

    public void testDeletedElementCanBeOmitted() {
        final String expectedXml = ""
            + "<thing>\n"
            + "  <meanwhileDeletedIgnore>c</meanwhileDeletedIgnore>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "meanwhileDeletedIgnore");

        final Thing out = xstream.fromXML(expectedXml);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testDeletedElementWithReferenceCanBeOmitted() {
        final String expectedXml = ""
            + "<thing>\n"
            + "  <meanwhileDeletedIgnore reference=\"..\"/>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "meanwhileDeletedIgnore");

        final Thing out = xstream.fromXML(expectedXml);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testDeletedElementWithClassAttributeCanBeOmitted() {
        final String expectedXml = ""
            + "<thing>\n"
            + "  <meanwhileDeletedIgnore class=\"thing\"/>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "meanwhileDeletedIgnore");

        final Thing out = xstream.fromXML(expectedXml);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testDeletedAttributeCanBeOmitted() {
        final String expectedXml = ""
            + "<thing meanwhileDeletedIgnore='c'>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "meanwhileDeletedIgnore");

        final Thing out = xstream.fromXML(expectedXml);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testAttributeCanBeOmitted() {
        final String expectedXml = "<thing neverIgnore=\"c\"/>";

        xstream.alias("thing", Thing.class);
        xstream.useAttributeFor(String.class);
        xstream.omitField(Thing.class, "sometimesIgnore");

        final Thing in = new Thing();
        in.alwaysIgnore = "a";
        in.sometimesIgnore = "b";
        in.neverIgnore = "c";
        assertEquals(expectedXml, xstream.toXML(in));

        final Thing out = xstream.fromXML(expectedXml);
        assertNull(out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testExistingFieldsCanBeExplicitlyOmittedAtDeserialization() {
        final String actualXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>foo</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        xstream.alias("thing", Thing.class);
        xstream.omitField(Thing.class, "sometimesIgnore");

        final Thing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals(null, out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
    }

    public void testExistingFieldsInDerivedClassesCanBeExplicitlyOmittedAtDeserialization() {
        final String actualXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "  <derived>foo</derived>\n"
            + "</thing>";

        xstream.alias("thing", DerivedThing.class);
        xstream.omitField(DerivedThing.class, "derived");

        final DerivedThing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals("b", out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
        assertEquals(null, out.derived);
    }

    public void testExistingInheritedFieldsCanBeExplicitlyOmittedAtDeserialization() {
        final String actualXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>foo</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "  <derived>d</derived>\n"
            + "</thing>";

        xstream.alias("thing", DerivedThing.class);
        xstream.omitField(Thing.class, "sometimesIgnore");

        final DerivedThing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals(null, out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
        assertEquals("d", out.derived);
    }

    public void testIgnoreUnknownElementsMatchingPattern() {
        final String actualXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>foo</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "  <foobar>f</foobar>\n"
            + "  <derived>d</derived>\n"
            + "</thing>";

        xstream.alias("thing", DerivedThing.class);
        xstream.omitField(Thing.class, "sometimesIgnore");
        xstream.ignoreUnknownElements("foo.*");

        final DerivedThing out = xstream.fromXML(actualXml);
        assertEquals(null, out.alwaysIgnore);
        assertEquals(null, out.sometimesIgnore);
        assertEquals("c", out.neverIgnore);
        assertEquals("d", out.derived);

        try {
            xstream.fromXML(actualXml.replaceAll("foobar", "unknown"));
            fail("Thrown " + ConversionException.class.getName() + " expected");
        } catch (final ConversionException e) {
            final String message = e.getMessage();
            assertTrue(message, e.getMessage().substring(0, message.indexOf('\n')).endsWith(DerivedThing.class.getName()
                + ".unknown"));
        }
    }

    public void testIgnoreNonExistingElementsMatchingTypeAlias() {
        xstream.alias("thing", Thing.class);
        xstream.ignoreUnknownElements("string");
        final Thing thing = new Thing();
        final String provided = "" //
                + "<thing>\n" + "  <string>string 1</string>\n" + "</thing>";
        final String expected = "<thing/>";
        assertWithAsymmetricalXml(thing, provided, expected);
    }

    public void testIgnoredElementIsNotInstantiated() {
        xstream.alias("thing", Thing.class);
        xstream.ignoreUnknownElements("int");
        final Thing thing = new Thing();
        final String provided = "" + "<thing>\n" + "  <int>invalid</int>\n" + "</thing>";
        final String expected = "<thing/>";
        assertWithAsymmetricalXml(thing, provided, expected);
    }

    static class ThingAgain extends Thing {
        private static final long serialVersionUID = 200712L;
        @SuppressWarnings("hiding")
        String sometimesIgnore;

        void setHidden(final String s) {
            super.sometimesIgnore = s;
        }

        String getHidden() {
            return super.sometimesIgnore;
        }
    }

    public void testAnOmittedFieldMakesADefinedInAttributeSuperfluous() {
        final ThingAgain in = new ThingAgain();
        in.alwaysIgnore = "a";
        in.setHidden("b");
        in.neverIgnore = "c";
        in.sometimesIgnore = "d";

        xstream.alias("thing", ThingAgain.class);
        xstream.omitField(ThingAgain.class, "sometimesIgnore");

        final String expectedXml = ""
            + "<thing>\n"
            + "  <sometimesIgnore>b</sometimesIgnore>\n"
            + "  <neverIgnore>c</neverIgnore>\n"
            + "</thing>";

        final String actualXml = xstream.toXML(in);
        assertEquals(expectedXml, actualXml);

        final ThingAgain out = xstream.fromXML(expectedXml);
        assertNull(out.alwaysIgnore);
        assertEquals("b", out.getHidden());
        assertEquals("c", out.neverIgnore);
        assertNull(out.sometimesIgnore);
    }
}
