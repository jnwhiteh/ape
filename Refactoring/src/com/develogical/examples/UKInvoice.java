package com.develogical.examples;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UKInvoice extends InvoiceBase {

    @Override
    protected String GetText() {
        for (LineItem item : items) {
        text.append(item.name).append(" ").append(item.price).append('\n');
                total = total.add(item.price);
                BigDecimal vat = item.price.multiply(new BigDecimal(0.2));
                text.append("VAT").append(" ").append(vat.setScale(2, RoundingMode.HALF_EVEN).toPlainString()).append('\n');
                total = total.add(vat);
        }
        text.append("Total").append(" ").append(total.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
        return text.toString();
    }
}
