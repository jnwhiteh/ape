package shopping.checkout;

import java.math.BigDecimal;

public class ReceiptFormatter {
	private final Printer printer;

	public ReceiptFormatter(Printer printer) {
		this.printer = printer;
	}
	
	public void printReceiptLine(Product product, int count, BigDecimal lineTotal) {
		printer.print(count + " " + product.name() + " @ "
				+ product.unitPrice() + " each = " + lineTotal + "\n");
	}
	
	public void printTotalLine(BigDecimal total) {
		printer.print("Total = " + total + "\n");
	}
	
	public void endOfReceipt() {
		printer.feed();
	}

    public void printDiscountLine(Product product) {
        printer.print("Discount: 1 " + product.name() + " @ "
				+ product.unitPrice() + " each = -" + product.unitPrice() + "\n");
    }
}