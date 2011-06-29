package com.develogical.shopping;

public class EmailOrderTracker implements OrderTracker {
    private Recipient recipient;
    private EmailSender emailSender;

    public EmailOrderTracker(Recipient recipient, EmailSender emailSender) {
        this.recipient = recipient;
        this.emailSender = emailSender;
    }

    public void orderConfirmed(Order order) {
        emailSender.send(recipient.emailAddress(), String.format("Your order for '%s' is confirmed", order.description()));
    }

    public void outOfStock(Order order) {
    }
}
