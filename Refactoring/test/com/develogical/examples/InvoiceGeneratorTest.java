package com.develogical.examples;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class InvoiceGeneratorTest {

    @Test
    public void totalsUpPricesOfItems() {
        InvoiceGenerator generator = new InvoiceGenerator();

        generator.addLineItem(new LineItem("item1", new BigDecimal("100.00")));
        generator.addLineItem(new LineItem("item2", new BigDecimal("150.00")));

        String invoiceText = generator.getInvoiceText();

        assertThat(invoiceText, containsString("Total 250.00"));
    }

    @Test
    public void doesNotAddVatIfNoCountrySpecified() {
        InvoiceGenerator generator = new InvoiceGenerator();

        generator.addLineItem(new LineItem("item1", new BigDecimal("100.00")));
        String invoiceText = generator.getInvoiceText();

        assertThat(invoiceText, not(containsString("VAT")));
    }

    @Test
    public void ukVatIsAddedAtTwentyPercent() {
        InvoiceGenerator generator = new InvoiceGenerator();
        generator.setCountry(InvoiceGenerator.UK);

        generator.addLineItem(new LineItem("item", new BigDecimal("100.00")));

        String invoiceText = generator.getInvoiceText();

        assertThat(invoiceText, containsString("VAT 20.00"));
        assertThat(invoiceText, containsString("Total 120.00"));
    }
    
    @Test
    public void frenchVatIsAddedAtNineteenPointSixPercent() {
        InvoiceGenerator generator = new InvoiceGenerator();
        generator.setCountry(InvoiceGenerator.FRANCE);

        generator.addLineItem(new LineItem("item", new BigDecimal("100.00")));

        String invoiceText = generator.getInvoiceText();

        assertThat(invoiceText, containsString("VAT 19.60"));
        assertThat(invoiceText, containsString("Total 119.60"));
    }
}
