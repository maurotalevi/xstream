/*
 * Copyright (C) 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2018, 2020 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 04. June 2006 by Joe Walnes
 */
package com.thoughtworks.xstream.io.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;


public class TokenTest extends TestCase {

    private Token.Formatter tokenFormatter;
    private ByteArrayOutputStream buffer;
    private DataOutput out;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tokenFormatter = new Token.Formatter();
        buffer = new ByteArrayOutputStream();
        out = new DataOutputStream(buffer);
    }

    public void testDoesNotSupportNegativeIds() {
        final Token.StartNode token = new Token.StartNode(-5);
        try {
            writeOneToken(token);
            fail("Expected exception");
        } catch (final IOException expectedException) {
            // expected exception
        }
    }

    public void testUsesOneExtraByteForIdsThatCanBeRepresentedAsByte() throws IOException {
        final Token.StartNode token = new Token.StartNode(255);
        writeOneToken(token);
        assertEquals(2, buffer.size()); // One byte already written for token type.
        assertEquals(token, readOneToken());
    }

    public void testUsesTwoExtraBytesForIdsThatCanBeRepresentedAsShort() throws IOException {
        final Token.StartNode token = new Token.StartNode(30000);
        writeOneToken(token);
        assertEquals(3, buffer.size()); // One byte already written for token type.
        assertEquals(token, readOneToken());
    }

    public void testUsesFourExtraBytesForIdsThatCanBeRepresentedAsShort() throws IOException {
        final Token.StartNode token = new Token.StartNode(Integer.MAX_VALUE);
        writeOneToken(token);
        assertEquals(5, buffer.size()); // One byte already written for token type.
        assertEquals(token, readOneToken());
    }

    public void testUsesEightExtraBytesForIdsThatCanBeRepresentedAsLong() throws IOException {
        final Token.StartNode token = new Token.StartNode(324234325543L);
        writeOneToken(token);
        assertEquals(9, buffer.size()); // One byte already written for token type.
        assertEquals(token, readOneToken());
    }

    public void testUsesOneExtraByteForUtf8StringsWith1ByteCharacters() throws IOException {
        final Token.Value token = new Token.Value("12345");
        writeOneToken(token);
        assertEquals(8, buffer.size()); // One byte already written for token type and two for the length.
        assertEquals(token, readOneToken());
    }

    public void testUsesOneExtraByteForUtf8StringsWith2ByteCharacters() throws IOException {
        final Token.Value token = new Token.Value("\u0391\u03b8\u03ae\u03bd\u03b1"); // Athens
        writeOneToken(token);
        assertEquals(13, buffer.size()); // One byte already written for token type and two for the length.
        assertEquals(token, readOneToken());
    }

    public void testUsesIdForStringsWithMoreThen64KBytes() throws IOException {
        final StringBuffer builder = new StringBuffer(40000);
        for (int i = 0; i++ < 8000;) {
            builder.append("\u0391\u03b8\u03ae\u03bd\u03b1"); // Athens
        }
        final String string = builder.toString();
        assertEquals(40000, string.length()); // 5 chars, but each char 2 bytes in UTF-8
        final Token.Value token = new Token.Value(string);
        writeOneToken(token);
        assertEquals(80014, buffer.size()); // > 65k
        assertEquals(token, readOneToken());
    }

    private Token readOneToken() throws IOException {
        return tokenFormatter.read(new DataInputStream(new ByteArrayInputStream(buffer.toByteArray())));
    }

    private void writeOneToken(final Token token) throws IOException {
        tokenFormatter.write(out, token);
    }

}
