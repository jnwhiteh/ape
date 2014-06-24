package com.develogical.shopping;

public interface OrderTracker {
    void orderConfirmed(Order order);
    void outOfStock(Order order);
}
