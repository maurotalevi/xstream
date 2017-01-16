/*
 * Copyright (C) 2017 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 13. January 2017 by Matej Cimbora
 */
package com.thoughtworks.xstream.converters.extended;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Converts a {@link LocalDateTime} to a string.
 *
 * @author Matej Cimbora
 */
public class LocalDateTimeConverter implements SingleValueConverter {

    private static final DateTimeFormatter FORMATTER;

    static {
        FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return type.equals(LocalDateTime.class);
    }

    @Override
    public String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        LocalDateTime localDateTime = (LocalDateTime) obj;

        return FORMATTER.format(localDateTime);
    }

    @Override
    public Object fromString(String str) {
        try {
            return LocalDateTime.parse(str);
        } catch (DateTimeParseException e) {
            ConversionException exception = new ConversionException("Cannot parse string");
            exception.add("string", str);
            exception.add("targetType", LocalDateTime.class.getSimpleName());
            throw exception;
        }
    }

}
