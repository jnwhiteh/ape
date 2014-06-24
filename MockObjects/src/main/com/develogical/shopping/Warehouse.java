package com.develogical.shopping;

public interface Warehouse {
    boolean hasStockOf(Order order);

    Item retrieve(Order order);
}
