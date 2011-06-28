package com.develogical.examples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class InvoiceBase {

    protected StringBuilder text = new StringBuilder();
    protected List<LineItem> items = new ArrayList<LineItem>();
    protected BigDecimal total = new BigDecimal(0);

    protected abstract String GetText();

    protected void addLineItem(LineItem item) {
        items.add(item);
    }
}
