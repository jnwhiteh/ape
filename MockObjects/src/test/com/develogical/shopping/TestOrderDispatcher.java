package com.develogical.shopping;

import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TestOrderDispatcher {

    @Before
    public void stockWarehouse() {

    }

    @Test
    public void OnOrderItemWithInStockItemShouldConfirmOrder() {

          OrderDispatcher orderDispatcher = new OrderDispatcher();


    }

    @Test
    public void ifItemIsNotInStockThenOrderShouldBeMarkedOutOfStock() {


    }

    @Test
    public void packetsAreNotSentOutForOutOfStockItems() {


    }

    @Test
    public void twoOrdersPlacedByDifferentRecipientsAreSentAsTwoSeparatePackets() {


    }

    @Test
    public void twoOrdersByTheSamePersonPlacedBeforeTheVehicleArrivesAreCombinedIntoOnePacket() {


    }

    @Test
    public void ifTheTruckArrivesBeforeYourSecondOrderIsPlacedYouWillReceiveTwoSeparatePackets() {


    }

    @Test
    public void ifTheTruckDepartsBeforeYourSecondOrderIsPlacedYouWillReceiveTwoSeparateDeliveries() {


    }
}
