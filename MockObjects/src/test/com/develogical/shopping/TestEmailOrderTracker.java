package com.develogical.shopping;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TestEmailOrderTracker {

    Mockery context = new Mockery();

    EmailSender emailSender = context.mock(EmailSender.class);
    
    Recipient johnSmith = new Recipient("John Smith", "99 Acacia Avenue, London", "john@example.com");
    OrderTracker tracker = new EmailOrderTracker(johnSmith, emailSender);

    Order order = new Order("Some Book");

    @Test
    public void ordersAreConfirmedByEmail() {

        context.checking(new Expectations() {{
            one(emailSender).send("john@example.com", "Your order for 'Some Book' is confirmed");
        }});

        tracker.orderConfirmed(order);
    }


    @Test
    public void outOfStockItemsDoNotSendAnyEmails() {

        context.checking(new Expectations() {{
            never(emailSender);
        }});

        tracker.outOfStock(order);
    }

}
