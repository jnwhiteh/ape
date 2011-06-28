package com.develogical.examples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InvoiceGenerator {
    public static final int UK = 1;
    public static final int FRANCE = 2;
    
    private Integer country;
    private List<LineItem> items = new ArrayList<LineItem>();

    public String getInvoiceText() {
        StringBuilder text = new StringBuilder();
        BigDecimal total = new BigDecimal(0);
        for (LineItem item : items) {
            if (country == null) {
                text.append(item.name).append(" ").append(item.price).append('\n');
                total = total.add(item.price);
            } else if (country == UK) {
                text.append(item.name).append(" ").append(item.price).append('\n');
                total = total.add(item.price);
                BigDecimal vat = item.price.multiply(new BigDecimal(0.2));
                text.append("VAT").append(" ").append(vat.setScale(2, RoundingMode.HALF_EVEN).toPlainString()).append('\n');
                total = total.add(vat);
            } else if (country == FRANCE) {
                BigDecimal vat = item.price.multiply(new BigDecimal(0.196));
                text.append(item.name).append(" ").append(item.price).append('\n');
                text.append("VAT").append(" ").append(vat.setScale(2, RoundingMode.HALF_EVEN).toPlainString()).append('\n');
                total = total.add(item.price);
                total = total.add(vat);
            }
        }
        text.append("Total").append(" ").append(total.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
        return text.toString();
    }
    
    public void setCountry(int country) {
        this.country = country;
    }

    public void addLineItem(LineItem item) {
        items.add(item);
    }
}
