/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2011, 2014, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. September 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.io.xml;

import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.JDOMFactory;

import com.thoughtworks.xstream.io.naming.NameCoder;


/**
 * @author Laurent Bihanic
 */
public class JDomWriter extends AbstractDocumentWriter<Element, Element> {

    private final JDOMFactory documentFactory;

    /**
	 * @param container
	 * @param nameCoder
	 * @param factory
     * @since 1.4
     */
    public JDomWriter(final Element container, final JDOMFactory factory, final NameCoder nameCoder) {
        super(container, nameCoder);
        documentFactory = factory;
    }

    /**
	 * @param container
	 * @param replacer
	 * @param factory
     * @since 1.2
     * @deprecated As of 1.4 use {@link JDomWriter#JDomWriter(Element, JDOMFactory, NameCoder)} instead.
     */
    @Deprecated
    public JDomWriter(final Element container, final JDOMFactory factory, final XmlFriendlyReplacer replacer) {
        this(container, factory, (NameCoder)replacer);
    }

    public JDomWriter(final Element container, final JDOMFactory factory) {
        this(container, factory, new XmlFriendlyNameCoder());
    }

    /**
	 * @param factory
	 * @param nameCoder
     * @since 1.4
     */
    public JDomWriter(final JDOMFactory factory, final NameCoder nameCoder) {
        this(null, factory, nameCoder);
    }

    /**
	 * @param factory
	 * @param replacer
     * @since 1.2.1
     * @deprecated As of 1.4 use {@link JDomWriter#JDomWriter(JDOMFactory, NameCoder)} instead.
     */
    @Deprecated
    public JDomWriter(final JDOMFactory factory, final XmlFriendlyReplacer replacer) {
        this(null, factory, (NameCoder)replacer);
    }

    public JDomWriter(final JDOMFactory factory) {
        this(null, factory);
    }

    /**
	 * @param container
	 * @param nameCoder
     * @since 1.4
     */
    public JDomWriter(final Element container, final NameCoder nameCoder) {
        this(container, new DefaultJDOMFactory(), nameCoder);
    }

    /**
	 * @param container
	 * @param replacer
     * @since 1.2.1
     * @deprecated As of 1.4 use {@link JDomWriter#JDomWriter(Element, NameCoder)} instead.
     */
    @Deprecated
    public JDomWriter(final Element container, final XmlFriendlyReplacer replacer) {
        this(container, new DefaultJDOMFactory(), (NameCoder)replacer);
    }

    public JDomWriter(final Element container) {
        this(container, new DefaultJDOMFactory());
    }

    public JDomWriter() {
        this(new DefaultJDOMFactory());
    }

    @Override
    protected Element createNode(final String name) {
        final Element element = documentFactory.element(encodeNode(name));
        final Element parent = top();
        if (parent != null) {
            parent.addContent(element);
        }
        return element;
    }

    @Override
    public void setValue(final String text) {
        top().addContent(documentFactory.text(text));
    }

    @Override
    public void addAttribute(final String key, final String value) {
        top().setAttribute(documentFactory.attribute(encodeAttribute(key), value));
    }

    private Element top() {
        return getCurrent();
    }
}
