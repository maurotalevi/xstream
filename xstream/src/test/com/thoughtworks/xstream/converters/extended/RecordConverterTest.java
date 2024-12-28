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

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import junit.framework.Assert;


/**
 * Unit test for Record serialisation/deserialisation.
 *
 * @author <a href="mailto:julia.boes@oracle.com">Julia Boes</a>
 * @author <a href="mailto:chris.hegarty@oracle.com">Chris Hegarty</a>
 */
public class RecordConverterTest extends AbstractAcceptanceTest {

    /** Test where the single object is a record. */
    public record RecordRectangle(String height, int width, long x, double y) {}

    static final RecordRectangle RR = new RecordRectangle("ten", 20, 5L, 1.0);
    static final String RR_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordRectangle>
              <height>ten</height>
              <width>20</width>
              <x>5</x>
              <y>1.0</y>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordRectangle>
            """.strip();

    public void testBasicRecord() {
        final String xml = RR_XML;
        final var expectedObject = RR;
        assertBothWays(expectedObject, xml);
    }

    /** Test where the component record object is an array of records. */
    public record RecordWithArray(RecordRectangle[] recordArray) {}

    static final RecordWithArray RWA = new RecordWithArray(new RecordRectangle[]{RR});
    static final String RWA_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordWithArray>
              <recordArray>
                <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordRectangle>
                  <height>ten</height>
                  <width>20</width>
                  <x>5</x>
                  <y>1.0</y>
                </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordRectangle>
              </recordArray>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordWithArray>
            """.strip();

    public void testRecordWithArray() {
        final String xml = RWA_XML;
        final var expectedObject = RWA;
        assertBothWays(expectedObject, xml);
    }

    /** Test where the component record object is the declared type. */
    public record RecordOfRecord(RecordRectangle r) {}

    static final RecordRectangle RR2 = new RecordRectangle("five", 6, 7L, 8.9);
    static final RecordOfRecord ROR = new RecordOfRecord(RR2);
    static final String ROR_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOfRecord>
              <r class="com.thoughtworks.xstream.converters.extended.RecordConverterTest$RecordRectangle">
                <height>five</height>
                <width>6</width>
                <x>7</x>
                <y>8.9</y>
              </r>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOfRecord>
            """.strip();

    public void testRecordOfRecord() {
        final String xml = ROR_XML;
        final var expectedObject = ROR;
        assertBothWays(expectedObject, xml);
    }

    /** Test where the component record object is NOT the declared type. */
    public interface Larry {
        int i();
    }

    public record Moe(int i) implements Larry {}

    public record Curly(Larry aLarry) {}

    static final Curly CURLY = new Curly(new Moe(5));
    static final String CURLY_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Curly>
              <aLarry class="com.thoughtworks.xstream.converters.extended.RecordConverterTest$Moe">
                <i>5</i>
              </aLarry>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Curly>
            """.strip();

    public void testRecordOfRecordWithSubtypes() {
        final String xml = CURLY_XML;
        final var expectedObject = CURLY;
        assertBothWays(expectedObject, xml);
    }

    /** Test where the record has an explicit constructor. */
    public record RecordWithConstructor(String height, int width, long x, double y) {
        public RecordWithConstructor(String height) {
            this(height, 20, 5, 1.0);
        }
    }

    static final RecordWithConstructor RWC = new RecordWithConstructor("ten");
    static final String RWC_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordWithConstructor>
              <height>ten</height>
              <width>20</width>
              <x>5</x>
              <y>1.0</y>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordWithConstructor>
            """.strip();

    public void testRecordWithConstructor() {
        final String xml = RWC_XML;
        final var expectedObject = RWC;
        assertBothWays(expectedObject, xml);
    }

    /**
     * Test where non-primitive record component is omitted during serialisation. The components' default value null is
     * inserted during deserialisation.
     */
    public record RecordOmittedNonPrimitive(
            byte b, short s, int i, long l, float f, double d, char c, boolean bool, String str) {}

    static final RecordOmittedNonPrimitive RONP = new RecordOmittedNonPrimitive((byte)0, (short)0, 0, 0l, 0.0f, 0.0d,
        '\u0000', false, null);
    static final String RONP_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOmittedNonPrimitive>
              <b>0</b>
              <s>0</s>
              <i>0</i>
              <l>0</l>
              <f>0.0</f>
              <d>0.0</d>
              <c></c>
              <bool>false</bool>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOmittedNonPrimitive>
            """.strip();
    // String str is omitted because is is null

    public void testRecordOmittedNonPrimitive() {
        final String xml = RONP_XML;
        final var expectedObject = RONP;
        assertBothWays(expectedObject, xml);
    }

    /**
     * Test where primitive record component values are missing in the xml. In this case the default values of the
     * respective types are inserted during deserialisation.
     */
    public record RecordOmittedPrimitive(
            byte b, short s, int i, long l, float f, double d, char c, boolean bool, String str) {}

    static final RecordOmittedPrimitive ROP = new RecordOmittedPrimitive((byte)0, (short)0, 0, 0l, 0.0f, 0.0d, '\u0000',
        false, null);
    static final String ROP_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOmittedPrimitive>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-RecordOmittedPrimitive>
            """.strip();

    public void testDeserializeRecordOmittedPrimitive() {
        final String xml = ROP_XML;
        final var expectedObject = ROP;
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var fromXML = (RecordOmittedPrimitive)xstream.fromXML(xml);
        assertEquals(expectedObject, fromXML);
    }

    public record Empty() {}

    static final String EMPTY_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Empty/>
            """.strip();

    public void testEmpty() {
        final String xml = EMPTY_XML;
        final var expectedObject = new Empty();
        assertBothWays(expectedObject, xml);
    }

    static final String EMPTY_XML_2 = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Empty>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Empty>
            """.strip();

    public void testEmpty2() {
        final String xml = EMPTY_XML_2;
        final var expectedObject = new Empty();
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var fromXML = (Empty)xstream.fromXML(xml);
        assertEquals(expectedObject, fromXML);
    }

    public record Point(int x, int y) {}

    static final String POINT_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Point>
              <y>2</y>
              <x>1</x>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-Point>
            """.strip();
    // values of x and y reversed

    public void testDeserializeOfPoint() {
        final String xml = POINT_XML;
        final var expectedObject = new Point(1, 2);
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var fromXML = (Point)xstream.fromXML(xml);
        assertEquals(expectedObject, fromXML);
    }

    public void testArrayOfPoints() {
        Point[] points = new Point[100];
        IntStream.range(0, 100).forEach(i -> points[i] = new Point(i, i + 100));
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var deserPoints = (Point[])xstream.fromXML(xstream.toXML(points));
        IntStream.range(0, 100).forEach(i -> assertEquals(points[i], deserPoints[i]));
    }

    public record PositivePoint(int x, int y) {
        public PositivePoint { // compact syntax
            if (x < 0)
                throw new IllegalArgumentException("negative x:" + x);
            if (y < 0)
                throw new IllegalArgumentException("negative y:" + y);
        }
    }

    static final String POSITIVE_POINT_BAD_X_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-PositivePoint>
              <x>-1</x>
              <y>2</y>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-PositivePoint>
            """.strip();
    // the value of x is negative (illegal) !

    public void testDeserializeOfPositivePointBadX() {
        final String xml = POSITIVE_POINT_BAD_X_XML;
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var e = expectThrows(IllegalArgumentException.class, () -> xstream.fromXML(xml));
        assertEquals("negative x:-1", e.getMessage());
    }

    static final String POSITIVE_POINT_BAD_Y_XML = """
            <com.thoughtworks.xstream.converters.extended.RecordConverterTest_-PositivePoint>
              <x>1</x>
              <y>-2</y>
            </com.thoughtworks.xstream.converters.extended.RecordConverterTest_-PositivePoint>
            """.strip();
    // the value of y is negative (illegal) !

    public void testDeserializeOfPositivePointBadY() {
        final String xml = POSITIVE_POINT_BAD_Y_XML;
        final var xstream = new XStream();
        xstream.allowTypesByRegExp("com.thoughtworks.xstream.converters.extended.RecordConverterTest.*");
        var e = expectThrows(IllegalArgumentException.class, () -> xstream.fromXML(xml));
        assertEquals("negative y:-2", e.getMessage());
    }

    static <T extends Throwable> T expectThrows(Class<T> throwableClass, Runnable task) {
        try {
            task.run();
            throw new AssertionError("Exception not thrown");
        } catch (ConversionException ce) {
            Throwable cause = ce.getCause();
            if (!throwableClass.isInstance(cause)) {
                throw new RuntimeException("expected: " + throwableClass + ", actual: " + cause);
            }
            return throwableClass.cast(cause);
        }
    }

    public static final class MyObj
    {
        SerializableRecord field1 = new SerializableRecord( 1, 2 );
        Object field2 = new SerializableRecord( 1, 2 );
        Object field3 = new Object[] { new SerializableRecord( 1, 2 ) };

        NotSerializableRecord field4 = new NotSerializableRecord( 1, 2 );
        Object field5 = new NotSerializableRecord( 1, 2 );
        Object field6 = new Object[] { new NotSerializableRecord( 1, 2 ) };

        @Override
        public boolean equals(Object o)
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            final MyObj myObj = (MyObj) o;
            return Objects.equals( field1, myObj.field1 ) &&
                Objects.equals( field2, myObj.field2 ) &&
                Arrays.equals( (Object[]) field3, (Object[]) myObj.field3 ) &&
                Objects.equals( field4, myObj.field4 ) &&
                Objects.equals( field5, myObj.field5 ) &&
                Arrays.equals( (Object[]) field6, (Object[]) myObj.field6 );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( field1, field2, field3, field4, field5, field6 );
        }
    }

    public record NotSerializableRecord(int a, int b) {}
    public record SerializableRecord(int a, int b) implements Serializable {}

    public void testMissingClassAttributeDoesNotCauseCrash()
    {
        final MyObj in = new MyObj();

        final XStream xstream = new XStream( new StaxDriver() );
        xstream.addPermission( AnyTypePermission.ANY);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        xstream.toXML( in, bos );
        final String expected = """
<?xml version='1.0' encoding='UTF-8'?><com.thoughtworks.xstream.converters.extended.RecordConverterTest_-MyObj>
<field1><a>1</a><b>2</b></field1><field2 class="com.thoughtworks.xstream.converters.extended.RecordConverterTest$SerializableRecord">
<a>1</a><b>2</b></field2><field3 class="object-array">
<com.thoughtworks.xstream.converters.extended.RecordConverterTest_-SerializableRecord><a>1</a><b>2</b>
</com.thoughtworks.xstream.converters.extended.RecordConverterTest_-SerializableRecord></field3><field4><a>1</a>
<b>2</b></field4><field5 class="com.thoughtworks.xstream.converters.extended.RecordConverterTest$NotSerializableRecord">
<a>1</a><b>2</b></field5><field6 class="object-array">
<com.thoughtworks.xstream.converters.extended.RecordConverterTest_-NotSerializableRecord><a>1</a><b>2</b>
</com.thoughtworks.xstream.converters.extended.RecordConverterTest_-NotSerializableRecord></field6>
</com.thoughtworks.xstream.converters.extended.RecordConverterTest_-MyObj>
            """;
        final String actual = bos.toString( StandardCharsets.UTF_8 );
        Assert.assertEquals( expected.replace("\n", ""), actual );

        final Object out = xstream.fromXML( new ByteArrayInputStream( bos.toByteArray() ) );
        Assert.assertEquals( in, out );
    }
}
