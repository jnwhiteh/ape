package com.develogical.shopping;

public class OrderDispatcher  {


    private Warehouse _warehouse;
    private OrderTracker _orderTracker;
    private DeliveryVehicle _deliveryVehicle;

    public OrderDispatcher(Warehouse warehouse, OrderTracker orderTracker, DeliveryVehicle deliveryVehicle) {
        _warehouse = warehouse;
        _orderTracker = orderTracker;
        _deliveryVehicle = deliveryVehicle;
    }


    public void placeOrder(Recipient recipient, Order order) {

        boolean orderIsInStock = _warehouse.hasStockOf(order);
        if (orderIsInStock)
        {
            _orderTracker.orderConfirmed(order);
            Item item = _warehouse.retrieve(order);
            Parcel orderParcel = new Parcel(item);
            _deliveryVehicle.deliver(recipient, orderParcel);
        }
        if(!orderIsInStock)
            _orderTracker.outOfStock(order);
    }
}
