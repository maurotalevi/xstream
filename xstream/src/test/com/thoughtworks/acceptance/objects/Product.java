/*
 * Copyright (C) 2007, 2013, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 30. April 2007 by Joerg Schaible
 */
package com.thoughtworks.acceptance.objects;

import java.util.ArrayList;


public class Product {

    String name;
    String id;
    double price;
    ArrayList<?> tags;

    public Product(final String name, final String id, final double price) {
        super();
        this.name = name;
        this.id = id;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getTags() {
        return (ArrayList<T>)tags;
    }

    public <T> void setTags(final ArrayList<T> tags) {
        this.tags = tags;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        long temp;
        temp = Double.doubleToLongBits(price);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (tags == null ? 0 : tags.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Product other = (Product)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price)) {
            return false;
        }
        if (tags == null) {
            if (other.tags != null) {
                return false;
            }
        } else if (!tags.equals(other.tags)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String ret = "[" + name + ", " + id + ", " + price;
        if (tags != null) {
            ret += "\n{";
	    ret = tags.stream().map((tag) -> tag.toString() + "\n").reduce(ret, String::concat);
            ret += "}";
        }
        ret += "]";
        return ret;
    }

}
