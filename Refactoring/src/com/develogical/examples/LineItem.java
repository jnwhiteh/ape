package com.develogical.examples;

import java.math.BigDecimal;

public class LineItem {
    public String name;
    public BigDecimal price;

    public LineItem(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
}
