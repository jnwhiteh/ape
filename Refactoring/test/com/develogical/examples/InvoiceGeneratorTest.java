package com.develogical.examples;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class InvoiceGeneratorTest {

    @Test
    public void totalsUpPricesOfItems() {
         InvoiceBase invoice = InvoiceGenerator.BuildAsNotCountrySpecificInvoice();

        invoice.addLineItem(new LineItem("item1", new BigDecimal("100.00")));
        invoice.addLineItem(new LineItem("item2", new BigDecimal("150.00")));

        String invoiceText = invoice.GetText();

        assertThat(invoiceText, containsString("Total 250.00"));
    }

    @Test
    public void doesNotAddVatIfNoCountrySpecified() {
        InvoiceBase invoice = InvoiceGenerator.BuildAsNotCountrySpecificInvoice();

        invoice.addLineItem(new LineItem("item1", new BigDecimal("100.00")));
        String invoiceText = invoice.GetText();

        assertThat(invoiceText, not(containsString("VAT")));
    }

    @Test
    public void ukVatIsAddedAtTwentyPercent() {
        InvoiceBase invoice = InvoiceGenerator.BuildAsUKInvoice();

        invoice.addLineItem(new LineItem("item", new BigDecimal("100.00")));

        String invoiceText = invoice.GetText();

        assertThat(invoiceText, containsString("VAT 20.00"));
        assertThat(invoiceText, containsString("Total 120.00"));
    }
    
    @Test
    public void frenchVatIsAddedAtNineteenPointSixPercent() {
        InvoiceBase invoice = InvoiceGenerator.BuildAsFranceInvoice();
        invoice.addLineItem(new LineItem("item", new BigDecimal("100.00")));

        String invoiceText = invoice.GetText();

        assertThat(invoiceText, containsString("VAT 19.60"));
        assertThat(invoiceText, containsString("Total 119.60"));
    }
}
