package com.develogical.shopping;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TestOrderDispatcher {

    Mockery context = new Mockery();
    OrderTracker orderTracker;
    Warehouse warehouse;
    Recipient recipient = new Recipient("Jay Ratton", "66 Mistery Lane, London, XW1 0ZA", "jratton@shadowline.com");
    Recipient recipient2 = new Recipient("Harvey Ratton", "106 Cemetery Lane, London, OO1 0ZA", "hratton@shadowline.com");

    DeliveryVehicle deliveryVehicle;
    Book itemOrdered, itemOrdered2;

    Parcel parcel, parcel2;

    public TestOrderDispatcher() {
    }

    @Before
    public void stockWarehouse() {
        orderTracker = context.mock(OrderTracker.class);
        warehouse = context.mock(Warehouse.class);
        deliveryVehicle = context.mock(DeliveryVehicle.class);
        itemOrdered = new Book("calla lillies - how to stuff drugs in hollow stems");
        parcel = new Parcel(itemOrdered);


        itemOrdered2 = new Book("how to stuff drugs in hollow stems part 2");
        parcel2 = new Parcel(itemOrdered2);
    }

    @Test
    public void OnPlaceOrderWithInStockItemShouldConfirmOrder() {

        final Order order = new Order("FlowersOrder");

        context.checking(new Expectations() {{
            allowing(warehouse).hasStockOf(order);will(returnValue(true));
            allowing(warehouse).retrieve(order);will(returnValue(itemOrdered));
            allowing(deliveryVehicle).deliver(recipient, parcel);
             one(orderTracker).orderConfirmed(order);
        }});

        OrderDispatcher orderDispatcher = new OrderDispatcher(warehouse, orderTracker, deliveryVehicle);
        orderDispatcher.placeOrder(recipient, order);
    }


    @Test
    public void OnPlaceOrderWithItemOutOfStockMarksOrderAsOutOfStock() {
        final Order order = new Order("FlowersOrder");

        context.checking(new Expectations() {{
            allowing(warehouse).hasStockOf(order);will(returnValue(false));
             one(orderTracker).outOfStock(order);
        }});

        OrderDispatcher orderDispatcher = new OrderDispatcher(warehouse, orderTracker, deliveryVehicle);
        orderDispatcher.placeOrder(recipient, order);
    }


    @Test
    public void OnPlaceOrderWithInStockOrderDispatchesOrderToRecipient() {

        final Order order = new Order("FlowersOrder");

        context.checking(new Expectations() {{
            allowing(warehouse).hasStockOf(order);will(returnValue(true));
            allowing(orderTracker).orderConfirmed(order);
            allowing(warehouse).retrieve(order);will(returnValue(itemOrdered));
            one(deliveryVehicle).deliver(recipient, parcel);
        }});

        OrderDispatcher orderDispatcher = new OrderDispatcher(warehouse, orderTracker, deliveryVehicle);
        orderDispatcher.placeOrder(recipient, order);
    }

    @Test
     public void OnPlaceOrderByTwoDifferentRecipientsAreSentAsTwoSeparateParcels()
     {
         final Order order1 = new Order("FlowersOrder");
         final Order order2 = new Order("BooksOrder");

         context.checking(new Expectations() {{
            allowing(warehouse).hasStockOf(order1);will(returnValue(true));
            allowing(orderTracker).orderConfirmed(order1);
            allowing(warehouse).retrieve(order1);will(returnValue(itemOrdered));
            one(deliveryVehicle).deliver(recipient, parcel);
        }});

         context.checking(new Expectations() {{
            allowing(warehouse).hasStockOf(order2);will(returnValue(true));
            allowing(orderTracker).orderConfirmed(order2);
            allowing(warehouse).retrieve(order2);will(returnValue(itemOrdered2));
            one(deliveryVehicle).deliver(recipient2, parcel2);
        }});

         OrderDispatcher orderDispatcher = new OrderDispatcher(warehouse, orderTracker, deliveryVehicle);
         orderDispatcher.placeOrder(recipient, order1);
         orderDispatcher.placeOrder(recipient2, order2);
     }
}
