package com.develogical.examples;

import java.math.RoundingMode;

public class NotCountrySpecificInvoice extends InvoiceBase {

    @Override
    protected String GetText() {
        for (LineItem item : items) {

        text.append(item.name).append(" ").append(item.price).append('\n');
                        total = total.add(item.price);
        }

        text.append("Total").append(" ").append(total.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
        return text.toString();
    }
}
