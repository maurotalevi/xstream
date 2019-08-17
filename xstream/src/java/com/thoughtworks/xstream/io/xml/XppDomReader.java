/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2011, 2014 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. March 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.xppdom.XppDom;


/**
 * @author Jason van Zyl
 */
public class XppDomReader extends AbstractDocumentReader {

    private XppDom currentElement;

    public XppDomReader(final XppDom xppDom) {
        super(xppDom);
    }

    /**
	 * @param xppDom
	 * @param nameCoder
     * @since 1.4
     */
    public XppDomReader(final XppDom xppDom, final NameCoder nameCoder) {
        super(xppDom, nameCoder);
    }

    /**
	 * @param xppDom
	 * @param replacer
     * @since 1.2
     * @deprecated As of 1.4 use {@link XppDomReader#XppDomReader(XppDom, NameCoder)} instead.
     */
    @Deprecated
    public XppDomReader(final XppDom xppDom, final XmlFriendlyReplacer replacer) {
        this(xppDom, (NameCoder)replacer);
    }

    @Override
    public String getNodeName() {
        return decodeNode(currentElement.getName());
    }

    @Override
    public String getValue() {
        String text = null;

        try {
            text = currentElement.getValue();
        } catch (final Exception e) {
            // do nothing.
        }

        return text == null ? "" : text;
    }

    @Override
    public String getAttribute(final String attributeName) {
        return currentElement.getAttribute(encodeAttribute(attributeName));
    }

    @Override
    public String getAttribute(final int index) {
        return currentElement.getAttribute(currentElement.getAttributeNames()[index]);
    }

    @Override
    public int getAttributeCount() {
        return currentElement.getAttributeNames().length;
    }

    @Override
    public String getAttributeName(final int index) {
        return decodeAttribute(currentElement.getAttributeNames()[index]);
    }

    @Override
    protected Object getParent() {
        return currentElement.getParent();
    }

    @Override
    protected Object getChild(final int index) {
        return currentElement.getChild(index);
    }

    @Override
    protected int getChildCount() {
        return currentElement.getChildCount();
    }

    @Override
    protected void reassignCurrentElement(final Object current) {
        currentElement = (XppDom)current;
    }

    @Override
    public String peekNextChild() {
        if (currentElement.getChildCount() == 0) {
            return null;
        }
        return decodeNode(currentElement.getChild(0).getName());
    }

}
