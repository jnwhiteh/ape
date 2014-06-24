package com.develogical.examples;

public class InvoiceGenerator {

    public static InvoiceBase BuildAsUKInvoice()
    {
            return new UKInvoice();
    }

    public static InvoiceBase BuildAsFranceInvoice()
    {
        return new FranceInvoice();
    }

    public static InvoiceBase BuildAsNotCountrySpecificInvoice()
    {
        return new NotCountrySpecificInvoice();
    }
}
