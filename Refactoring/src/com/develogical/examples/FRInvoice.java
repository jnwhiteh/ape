package com.develogical.examples;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FRInvoice extends InvoiceBase{

    @Override
    protected String GetText() {
        for (LineItem item : items) {
                BigDecimal vat = item.price.multiply(new BigDecimal(0.196));
                text.append(item.name).append(" ").append(item.price).append('\n');
                text.append("VAT").append(" ").append(vat.setScale(2, RoundingMode.HALF_EVEN).toPlainString()).append('\n');
                total = total.add(item.price);
                total = total.add(vat);
        }
        text.append("Total").append(" ").append(total.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
        return text.toString();
    }
}
