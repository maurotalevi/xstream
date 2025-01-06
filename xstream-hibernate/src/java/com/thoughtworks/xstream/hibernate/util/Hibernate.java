/*
 * Copyright (C) 2012, 2013, 2014, 2018 XStream Committers.
 * All rights reserved.
 *
 * Created on 08.06.2012 by Joerg Schaible
 */
package com.thoughtworks.xstream.hibernate.util;

import org.hibernate.proxy.HibernateProxy;


/**
 * Utility class for Hibernate support.
 *
 * @author J&ouml;rg Schaible
 * @since 1.4.3
 */
public class Hibernate {
    /** <code>PersistentBag</code> contains Hibernate's PersistenBag class type. */
    public final static Class<?> PersistentBag = loadHibernateType("org.hibernate.collection.internal.PersistentBag");
    /** <code>PersistentList</code> contains Hibernate's PersistenList class type. */
    public final static Class<?> PersistentList = loadHibernateType("org.hibernate.collection.internal.PersistentList");
    /** <code>PersistentMap</code> contains Hibernate's PersistenMap class type. */
    public final static Class<?> PersistentMap = loadHibernateType("org.hibernate.collection.internal.PersistentMap");
    /** <code>PersistentSet</code> contains Hibernate's PersistenSet class type. */
    public final static Class<?> PersistentSet = loadHibernateType("org.hibernate.collection.internal.PersistentSet");
    /** <code>PersistentSortedMap</code> contains Hibernate's PersistenSortedMap class type. */
    public final static Class<?> PersistentSortedMap = loadHibernateType("org.hibernate.collection.internal.PersistentSortedMap");
    /** <code>PersistentSortedSet</code> contains Hibernate's PersistenSortedSet class type. */
    public final static Class<?> PersistentSortedSet = loadHibernateType("org.hibernate.collection.internal.PersistentSortedSet");
    /** <code>EnversList</code> contains the ListProxy class type for Hibernate Envers. */
    public final static Class<?> EnversList = loadHibernateEnversType("org.hibernate.envers.entities.mapper.relation.lazy.proxy.ListProxy");
    /** <code>EnversMap</code> contains the MapProxy class type for Hibernate Envers. */
    public final static Class<?> EnversMap = loadHibernateEnversType("org.hibernate.envers.entities.mapper.relation.lazy.proxy.MapProxy");
    /** <code>EnversSet</code> contains the SetProxy class type for Hibernate Envers. */
    public final static Class<?> EnversSet = loadHibernateEnversType("org.hibernate.envers.entities.mapper.relation.lazy.proxy.SetProxy");
    /** <code>EnversSortedMap</code> contains the SortedMapProxy class type for Hibernate Envers. */
    public final static Class<?> EnversSortedMap = loadHibernateEnversType("org.hibernate.envers.entities.mapper.relation.lazy.proxy.SortedMapProxy");
    /** <code>EnversSortedSet</code> contains the SortedSetProxy class type for Hibernate Envers. */
    public final static Class<?> EnversSortedSet = loadHibernateEnversType("org.hibernate.envers.entities.mapper.relation.lazy.proxy.SortedSetProxy");

    private static Class<?> loadHibernateType(final String name) {
        Class<?> result = tryToLoadClass(name);
        if( result == null ){
            result = tryToLoadClass(name.replaceFirst("\\.internal\\.", "."));
        }
        if( result == null){
            result = tryToLoadClass(name.replaceFirst("\\.internal\\.", ".spi."));
        }
        return result;
    }

    private static Class<?> tryToLoadClass(String name) {
        try {
            return HibernateProxy.class.getClassLoader().loadClass(name);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private static Class<?> loadHibernateEnversType(final String name) {
        Class<?> type = null;
        try {
            type = HibernateProxy.class.getClassLoader().loadClass(name);
        } catch (final ClassNotFoundException e) {
            // not available
        }
        return type;
    }
}
