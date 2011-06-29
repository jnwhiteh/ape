package com.develogical.shopping;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class Parcel {
    private List<Item> items;

    public Parcel(Item... items) {
        this.items = Lists.newArrayList(items);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
